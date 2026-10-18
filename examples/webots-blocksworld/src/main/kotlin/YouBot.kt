import com.cyberbotics.webots.controller.Motor
import com.cyberbotics.webots.controller.Supervisor
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

// Calibration knobs, from the Tower of Hanoi contest controller of Webots (hanoi.c), tune them on the real world.
private const val TIME_STEP = 32
private const val BOX_SIZE = 0.05
private const val PLATFORM_HEIGHT = 0.01
private const val SAFETY_MARGIN = 0.01
private const val HOVER_HEIGHT = 0.2
private const val HEIGHT_PER_STEP = 0.002
private const val GRIP_GAP = 0.04
private const val ARM2_VELOCITY = 0.5
private const val FINGERS_VELOCITY = 0.03
private const val SETTLE_TIME = 0.2

/** Forward distance between the second arm joint, with the arm facing forward, and the slots. */
const val ARM_REACH = 0.2

/** The second arm joint is this far ahead of the axis of the first; hanoi.c approximates it scaling x by 0.9. */
private const val ARM2_OFFSET = 0.033

private const val ARM1_LENGTH = 0.253
private const val ARM2_LENGTH = 0.155
private const val ARM3_LENGTH = 0.135
private const val ARM4_LENGTH = 0.081
private const val ARM5_LENGTH = 0.105
private const val GRIPPER_MAX = 0.025
private const val GRIPPER_OFFSET_WHEN_LOCKED = 0.021

/**
 * A KUKA youBot that stays in place and picks boxes in front of it, ported from Webots' `youbot_control` library.
 * Each method steps the simulation until its motion ends, so only the thread that steps Webots may call it.
 *
 * @property supervisor the youBot's controller, a supervisor to read where the boxes are.
 */
class YouBot(val supervisor: Supervisor = Supervisor()) {
    private val arm = (1..5).map { supervisor.getMotor("arm$it") }
    private val fingers: Motor = supervisor.getMotor("finger::left")

    init {
        arm[1].setVelocity(ARM2_VELOCITY)
        fingers.setVelocity(FINGERS_VELOCITY)
    }

    /**
     * Advances the simulation by one control step; throws when Webots quits or reverts the world.
     */
    fun step() {
        check(supervisor.step(TIME_STEP) != -1) { "Webots ended the simulation" }
    }

    /**
     * Steps the simulation for [seconds] of simulated time.
     */
    fun wait(seconds: Double) {
        val end = supervisor.time + seconds
        while (supervisor.time < end) step()
    }

    /**
     * Where the solids named after the DEFs in [defs] are, as x, y, z in the world.
     */
    fun positions(defs: Collection<String>): Map<String, DoubleArray> =
        defs.associateWith { supervisor.getFromDef(it).position }

    /**
     * Grips the box at [lateral] metres to the left of the robot, standing on [level] boxes; or, if not [grip],
     * releases the box held there.
     */
    fun gripBox(lateral: Double, level: Int, grip: Boolean) {
        val x = -lateral // arm_ik measures x to the right
        val z = SAFETY_MARGIN + PLATFORM_HEIGHT + (level + 1) * BOX_SIZE + if (grip) 0.0 else SAFETY_MARGIN
        inverseKinematics(x, ARM_REACH, HOVER_HEIGHT)
        if (grip) fingers.setPosition(GRIPPER_MAX)
        wait(1.0)
        var h = HOVER_HEIGHT
        while (h > z) {
            inverseKinematics(x, ARM_REACH, h)
            step()
            h -= HEIGHT_PER_STEP
        }
        wait(SETTLE_TIME)
        fingers.setPosition(if (grip) (GRIP_GAP - GRIPPER_OFFSET_WHEN_LOCKED) / 2 else GRIPPER_MAX)
        wait(1.0)
        while (h < HOVER_HEIGHT) {
            inverseKinematics(x, ARM_REACH, h)
            step()
            h += HEIGHT_PER_STEP
        }
    }

    /**
     * Points the gripper downwards at [x] to the right, [y] ahead of the second arm joint and [z] high (arm_ik).
     */
    private fun inverseKinematics(x: Double, y: Double, z: Double) {
        val y1 = hypot(x, y + ARM2_OFFSET) - ARM2_OFFSET
        val z1 = z + ARM4_LENGTH + ARM5_LENGTH - ARM1_LENGTH
        val a = ARM2_LENGTH
        val b = ARM3_LENGTH
        val c = sqrt(y1 * y1 + z1 * z1)
        val alpha = -atan2(x, y + ARM2_OFFSET)
        val beta = -(PI / 2 - acos((a * a + c * c - b * b) / (2 * a * c)) - atan(z1 / y1))
        val gamma = -(PI - acos((a * a + b * b - c * c) / (2 * a * b)))
        val delta = -(PI + beta + gamma)
        val epsilon = PI / 2 + alpha
        listOf(alpha, beta, gamma, delta, epsilon).forEachIndexed { i, angle -> arm[i].setPosition(angle) }
    }
}
