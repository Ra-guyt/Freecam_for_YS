# Contributing to Freecam for YS

Freecam for YSへの提案や修正ありがとうございます。このプロジェクトでは、撮影用として固定した安全制限、利用者のプライバシー、上流への帰属を優先します。

## 開発環境

- Java 21
- Git
- WindowsではPowerShell、Linux/macOSでは任意のターミナル

依存関係はGradle Wrapperから取得します。Windowsでは次を実行してください。

```powershell
java -version
.\gradlew.bat clean :common:test :fabric:build --console=plain
git diff --check
```

Linux/macOSでは `./gradlew` を使用します。成果物は `build/libs/` に作成されます。

## Pull Request

1. 変更の目的を1つに絞ります。
2. Java 21でビルドとテストを実行します。
3. 安全制限、MOD ID、Minecraft 1.21.10、Fabric対応を意図せず変えていないか確認します。
4. 変更理由、確認方法、確認できていないことをPull Requestへ記載します。
5. 必要な場合だけバージョンを更新します。

自動マージは行いません。Minecraft、Fabric、Loom、Cloth Configなどの更新は、互換性を確認して1件ずつ判断します。

## 安全制限を守る

次のような変更は受け付けません。

- 50ブロック制限、衝突判定、未読込チャンク判定を弱める変更
- 攻撃、採掘、設置、使用、エンティティ操作を可能にする変更
- ウォーターマークを消す、隠す、任意で無効にする変更
- 通常版FreecamやLegacyFreecamとの同時導入を可能にする変更
- テレメトリ、解析、更新確認、外部HTTP通信、Webhook、外部プロセス実行の追加

安全性に影響する提案は、先にIssueで相談してください。ただし脆弱性の詳細は公開Issueへ書かず、[SECURITY.md](SECURITY.md)に従ってください。

## 個人情報とログ

ソース、コミット、Issue、Pull Request、画像、ログへ、次の情報を含めないでください。

- 本名、個人メール、Minecraft UUID、プレイヤー名
- Windowsユーザー名やユーザーフォルダーなどの絶対パス
- IPアドレス、非公開のサーバー名・座標・内部構成
- APIキー、トークン、Webhook、秘密鍵、パスワード、接続文字列
- `.env`、クラッシュレポート全文、データベース、IDEやローカル実行設定

ログは問題に関係する最小部分だけを貼り、上記の値を `<削除済み>` に置き換えてください。GitHubのユーザー設定では、コミット用メールにID付き `users.noreply.github.com` を使用してください。

## 画像とバイナリ

`icon.png` とウォーターマーク画像を、管理者の確認なしに差し替えないでください。追加・更新する画像は、作者名、ローカルパス、コメント、XMP/EXIFなど不要なメタデータを確認します。

公開済みReleaseのJARを同じバージョン名のまま差し替えてはいけません。JARの内容が変わる場合は、確認後に新しいバージョンとして公開します。不要な実行ファイル、ネイティブライブラリ、スクリプトを成果物へ含めないでください。

## ライセンスと上流への帰属

[LICENSE](LICENSE) と [NOTICE.md](NOTICE.md) を維持してください。上流MinecraftFreecam/Freecamの作者名、公開メール、著作権表示、コミット帰属を削除または一括置換してはいけません。

Freecam (Fair Play) / LegacyFreecamの非公開ソースをコピーしないでください。新しく持ち込むコードや素材は、公開可能なライセンスと出典を示してください。
