package model

/**
 * A cell of the grid; (0, 0) is the top-left corner.
 *
 * @property x the column.
 * @property y the row.
 */
data class Pos(val x: Int, val y: Int) {
    /**
     * The next cell towards [direction].
     */
    operator fun plus(direction: Direction) = Pos(x + direction.dx, y + direction.dy)
}

/**
 * Where the robot can face.
 *
 * @property dx the column step when moving forward.
 * @property dy the row step when moving forward.
 */
enum class Direction(val dx: Int, val dy: Int) {
    /** Up. */
    NORTH(0, -1),

    /** Right. */
    EAST(1, 0),

    /** Down. */
    SOUTH(0, 1),

    /** Left. */
    WEST(-1, 0),
    ;

    /** The direction after turning left. */
    val left: Direction get() = entries[(ordinal + entries.size - 1) % entries.size]

    /** The direction after turning right. */
    val right: Direction get() = entries[(ordinal + 1) % entries.size]
}

/**
 * The squares the robot sees, relative to where it faces.
 */
enum class Square {
    /** Where the robot is. */
    HERE,

    /** On the robot's left. */
    LEFT,

    /** Ahead, on the left. */
    FORWARD_LEFT,

    /** In front of the robot. */
    FORWARD,

    /** Ahead, on the right. */
    FORWARD_RIGHT,

    /** On the robot's right. */
    RIGHT,
    ;

    /**
     * The cell of this square, for a robot at [position] facing [facing].
     */
    fun cellFrom(position: Pos, facing: Direction): Pos = when (this) {
        HERE -> position
        LEFT -> position + facing.left
        FORWARD_LEFT -> position + facing + facing.left
        FORWARD -> position + facing
        FORWARD_RIGHT -> position + facing + facing.right
        RIGHT -> position + facing.right
    }
}

/**
 * What a square holds; outside the map is an obstacle.
 */
enum class Content {
    /** A wall, or the edge of the map. */
    OBSTACLE,

    /** Another robot, which hides whether the square is dusty. */
    ROBOT,

    /** Dust to clean. */
    DUST,

    /** Nothing. */
    EMPTY,
}
