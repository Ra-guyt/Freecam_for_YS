# Safety design and verification

## Policy boundary

This fork is intended only for administrator-approved filming, streaming, and
content creation on YS Server. It reduces capabilities; it does not decide
whether a player's purpose is content production or reconnaissance.

Permission is specific to the exact administrator-designated Freecam for YS
version from the official release. Installing or using upstream MinecraftFreecam/Freecam,
LegacyFreecam, or another fork remains prohibited even for filming, streaming,
or content creation.

The fixed boundary is a sphere with radius 50 blocks around the real player's
current position. It is deliberately not configurable.

## Controls

| Risk | Enforced control |
| --- | --- |
| Camera scouting at long range | `CameraSafetyPolicy` rejects distances greater than 50 blocks in 3D |
| Continuous movement crossing the boundary | End-of-tick check restores the last safe camera position |
| Tripod/saved/API teleport outside the boundary | Destination is validated before applying it |
| Unloaded-area access | Destination chunk must already be loaded |
| Block clipping | Collision override mixin and collision configuration were removed; every direct destination is collision checked |
| Player movement while detached | Real player receives empty input; server physics remains active |
| Player freeze/knockback immunity | Freeze injections and configuration were removed |
| World interaction | Client action entry points and multiplayer game-mode entry points both reject attacks, mining, item use, placement, interaction, and pick-block |
| Camera-origin interaction | Target redirection and interaction-mode configuration were removed |
| Hidden vanilla visual information | Submersion and equipped-item overlay suppression were removed |
| Damage avoidance | Health loss always schedules immediate Freecam exit |
| Misidentification in recordings | Mandatory YS logo/text layer renders while Freecam or Tripod is active, including when vanilla HUD is hidden |
| Accidental coexistence | Fabric metadata conflicts with `freecam` and `legacy-freecam` |

Full Brightness remains available because the YS policy allows ordinary
brightness/night-vision mods. It does not make blocks transparent or highlight
ores/entities.

## Verification checklist

1. Run `clean :common:test :fabric:build` with Java 21.
2. Confirm the four boundary unit tests pass.
3. Run a client and verify F4 activation, movement collision, and automatic
   return at the 50-block boundary.
4. Verify attack, mining, right-click in air, block use, entity use, placement,
   and pick-block do nothing while active.
5. Verify Tripod and public move APIs accept an in-range loaded destination and
   reject an out-of-range destination.
6. Verify the watermark appears only while active, remains visible with F1, and
   uses the supplied YS logo.
7. Audit the remapped JAR metadata, nested dependencies, resources, and behavior.

## Non-goals and limits

- The client cannot prove a user's intent.
- The client cannot prevent post-production cropping or replacement of video.
- An in-range camera may still reveal something around a corner. Server policy
  remains the controlling restriction.
- A malicious second mod can modify Minecraft independently; this project only
  constrains the code and public operations shipped in this JAR.
