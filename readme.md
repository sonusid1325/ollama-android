# LAMI Mobile (Android) クライアント

ローカルで動く個人用アシスタント「LAMI（ラミィ）」の Android 向けクライアントです。**Jetpack Compose** で構築された表情付き UI を備え、ローカル LLM との接続はオプションとして選択できます。ネットワークに依存せず動作し、プライバシーを尊重したまま日常的なタスクを手元でこなせます。

## Features

- **軽量・高速**: モバイル向けに最適化されたミニマルな UI。
- **ローカル完結**: インターネット非依存で動作し、プライバシーを確保。
- **表情豊かなインターフェース**: スプライトアニメーションによるリアクションで操作が直感的。
- **接続先を選べる**: ローカル LLM 連携はオプション。オフラインでも基本機能は利用可能。

## Screenshots

### Home Screen
<img src="Screenshots/01.png" width="250" />  <img src="Screenshots/02.png" width="250" />

### New Chat
<img src="Screenshots/03.png" width="250" />  <img src="Screenshots/04.png" width="250" />

### Chat Interface
<img src="Screenshots/05.png" width="250" />  <img src="Screenshots/06.png" width="250" />

<img src="Screenshots/07.png" width="250" />  <img src="Screenshots/08.png" width="250" />

### Settings

<img src="Screenshots/10.png" width="250" />  
<img src="Screenshots/11.png" width="250" />  

## スプライトアニメーション（状態駆動）

LAMI は内部状態に応じてスプライトを切り替え、ユーザーへのフィードバックを視覚的に提示します。各状態はイベントドリブンに遷移し、UI 反応を統一的に管理します。

- **Idle**: 待機中。入力がない時の基本表情。
- **Thinking**: 入力を処理中。思案するアニメーションで進行状況を示唆。
- **TalkShort**: 短い応答を再生中。レスポンスが軽い場合に使用。
- **TalkLong**: 長めの応答を再生中。ストリーミング出力や説明が続くケース。
- **TalkCalm**: 穏やかなトーンで応答。落ち着いた会話モードを示す。
- **ErrorLight**: 軽微なエラー。リトライ可能な入力不備など。
- **ErrorHeavy**: 致命的エラー。接続不可やモデル異常時に強調。
- **Offline**: ネットワーク未接続またはモデル未起動を明示。

状態は単一のステートマシンで管理され、UI とバックエンドのイベントを疎結合に保つことで拡張性とテスト容易性を確保しています。

## スプライト調整の反映確認手順

「画像調整」タブで矩形を動かした結果がギャラリー・アニメタブに即座に伝搬することを確認するには、次の手順を実施してください。

1. `Settings > Sprite Debug` からキャンバスに入り、「画像調整」タブでフレーム #2 など任意の枠を数ピクセル動かします。
2. 同じ画面内の「アニメーション」タブに切り替え、該当フレームのプレビュー位置がそのまま反映されているか目視で確認します。
3. 「ステータス」タブのギャラリーで同じ表情を選び、`LamiStatusSprite` の表示位置が一致していることを再度確認します。
4. 必要に応じて「リセット」で元の 3x3 配置に戻し、再度 1〜3 を繰り返します。

## Installation

1. **Download** the latest APK from [GitHub Releases](#)。
2. **Install** the APK on your Android device。
3. **Launch the application** and start interacting with the LAMI assistant。

## Requirements

- Android 13 or higher
- Minimum 4GB RAM (Recommended: 6GB+ for better performance)
- （オプション）ローカル LLM 環境を用意する場合は、端末上でモデルが動作する設定を済ませてください。

## Usage

1. Open the application.
2. （任意）ローカル LLM への接続を有効化し、モデルをロード。
3. 新規チャットを開始するか、既存スレッドを再開。
4. スプライトの表情や通知を確認しつつ、必要に応じて設定を調整。

## 将来拡張

- **音声同期**: 音声合成のタイムスタンプと連動した口パク・まばたき表現。
- **感情表現の強化**: センチメント解析に基づく表情・ポーズの自動変化。
- **リッチな状態管理**: ユーザー行動や通知と連動した新規ステートの追加（例: Listening, Busy）。
- **プラグイン連携**: ローカル API や外部サービスと安全に統合できる拡張ポイントの提供。

利用者は音声や表情を通じたリッチな対話を、開発者はステートマシンとスプライトセットを拡張することで独自の体験を構築できます。

## Contributing

We welcome contributions! Feel free to **fork the repository** and submit **pull requests**.

### Contribution Guidelines
- Follow standard Android development best practices.
- Ensure UI/UX consistency with Jetpack Compose.
- Keep performance optimizations in mind.

## 開発環境セットアップ

ローカルでの確認と自動化フローに参加するための最小手順です。

1. 必要ツールのインストール
   ```bash
   pip install --upgrade pre-commit commitizen
   pre-commit install --hook-type pre-commit --hook-type commit-msg
   ```
2. Android SDK の準備（未設定の場合）
   ```bash
   sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```
3. テスト実行
   ```bash
   ./gradlew test
   ```
4. 変更前の自動フォーマット
   ```bash
   pre-commit run --all-files
   ```

GitHub Actions でも `./gradlew test` を実行する CI を用意しているため、プルリクエスト作成時に自動でユニットテストが走ります。

## License

This project is licensed under the **MIT License**.

---

Developed with ❤️ using Jetpack Compose for Android.
