# Tabigraphy

Kotlin Multiplatform + Compose Multiplatform で構築するアプリ。現時点では Android ターゲットのみ実装している。

## モジュール構成

- `:composeApp` — Android アプリのエントリポイント（Activity、DI セットアップなど）
- `:shared` — commonMain にドメインロジック・データモデルを置く KMP モジュール。現時点では空だが、将来 iOS/Web ターゲットを追加する際にロジックを共有できるよう Gradle モジュールとして分離している

## 技術スタック

- **Kotlin** 2.4.10 / **Compose Multiplatform** 1.12.0 / **AGP** 9.3.0
- **[MapLibre Compose](https://maplibre.org/maplibre-compose/)** 0.15.0 — 地図表示。タイルスタイルは [MapTiler](https://www.maptiler.com/) から配信する
- **[Room](https://developer.android.com/jetpack/androidx/releases/room3)** 3.0.2 (`androidx.room3`) — KMP 対応の永続化ライブラリ。現時点では依存関係の疎通確認のみで、DB スキーマは未実装
- **KSP** 2.3.11 — Room のコード生成に使用

## compileSdk について

MapLibre Compose 0.15.0 / Compose Multiplatform 1.12.0 が推移的に依存する androidx.compose.ui などが API 37 でのコンパイルを要求するが、2026-08 時点で API 37 の正式版はまだリリースされておらず、Android SDK には preview チャンネルの `CANARY` (api-level 37.1) としてのみ存在する。そのため `compileSdkPreview = "CANARY"` を使用している（`targetSdk` は安定版の 36 のまま）。API 37 の正式版がリリースされ次第、`compileSdk = 37` への切り替えを検討すること。

`platforms;android-CANARY` と `build-tools;36.0.0` は Android Studio の SDK Manager からは `--include_obsolete`/preview チャンネル表示が必要な場合がある。`sdkmanager --install "platforms;android-CANARY" "build-tools;36.0.0"` で取得できない場合は、[Google の SDK リポジトリ](https://dl.google.com/android/repository/repository2-3.xml) から該当アーカイブを直接取得して配置すること。

## セットアップ

1. `local.properties.sample` を `local.properties` にコピーし、`maptiler.apiKey` に [MapTiler](https://cloud.maptiler.com/account/keys/) の API キーを設定する（`local.properties` は Git 管理対象外）
2. `./gradlew :composeApp:assembleDebug` でビルドできることを確認する

API キー未設定でもビルド自体は通り、実行時にスタイル URL のキー部分が空文字列になる。
