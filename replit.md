# ELDER LAUNCHER

An Android Studio fork of PojavLauncher with a Compose-based dark neon launcher UI for Minecraft Java and installed Bedrock.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server (port 5000)
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Required env: `DATABASE_URL` — Postgres connection string
- Open `android/` in Android Studio and build the `MainApp` debug configuration.

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)
- Android: Kotlin, Jetpack Compose Material 3, Room, Android SAF, Pojav JRE/LWJGL/native OpenGL bridge

## Where things live

- `android/app_pojavlauncher` — MainApp and Compose UI
- `android/core` — Core Android library boundary
- `android/jre_lwjgl3glfw` — JRE module
- `android/app_pojavlauncher/src/main/jni` and `jniLibs` — OpenGL/native bridge
- `android/app_pojavlauncher/src/main/java/com/elder/launcher` — ELDER UI, Room instance store, Mojang metadata client

## Architecture decisions

- Keep Pojav's upstream Java/native launch pipeline intact; the Compose shell routes into it for account, download, mod-loader, touch-control, and game-launch behavior.
- Use applicationId `com.elder.launcher` while retaining Pojav's source namespace so the upstream resource and native bridge references remain compatible.
- Persist launcher instances with Room; persist the Bedrock tree URI with Android SAF permissions.

## Product

ELDER LAUNCHER provides Home, Instances, Library, Tools, and Account screens with a dark `#0A0A0A` surface, neon `#00FF66` accent, Compose Material 3 components, Minecraft Java launch/auth/install actions, live Mojang version metadata, a Room-backed instance list, RAM controls, touch-control entry points, and installed Bedrock launching.

## User preferences

The user requested the attached ELDER LAUNCHER visual identity: dark black surfaces, neon green accents, blue EL icon, and a Minecraft-focused mobile launcher experience.

## Gotchas

The Android SDK is not installed in the current Replit shell, so APK compilation must be run from Android Studio or another Android SDK-equipped environment.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
