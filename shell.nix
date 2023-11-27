{
	pkgs ? import <nixpkgs> {}
}:

(
	pkgs.buildFHSUserEnv
	{
		name = "idea-ultimate with jdk17";
		targetPkgs = pkgs:
		[
			pkgs.jetbrains.idea-ultimate
			pkgs.jdk17
      pkgs.kotlin
		];
	}
).env
