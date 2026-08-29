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

## セットアップ

1. `local.properties.sample` を `local.properties` にコピーし、`maptiler.apiKey` に [MapTiler](https://cloud.maptiler.com/account/keys/) の API キーを設定する（`local.properties` は Git 管理対象外）
2. `./gradlew :composeApp:assembleDebug` でビルドできることを確認する

API キー未設定でもビルド自体は通り、実行時にスタイル URL のキー部分が空文字列になる。
