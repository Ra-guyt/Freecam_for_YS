# Freecam for YS

## 1. Freecam for YSとは

YSサーバーでの撮影・配信・コンテンツ制作のために、安全制限を固定したFabricクライアントMODです。
[MinecraftFreecam/Freecam](https://github.com/MinecraftFreecam/Freecam) のMITライセンス版を基にしています。

## 2. 最重要の利用条件

> [!IMPORTANT]
> YSサーバーで使えるのは、管理者が指定した正しいバージョンの **Freecam for YS** だけです。
> **撮影・配信・コンテンツ制作の目的に限って使用できます。**
>
> 同じ目的でも、通常版MinecraftFreecam/Freecam、LegacyFreecam、その他のForkは導入・使用できません。
> 探索、偵察、情報収集、隠し場所や他プレイヤーの捜索にも使用できません。

公開されていること自体は、サーバーでの使用許可を意味しません。
[サーバールール](https://www.raagu.info/wiki/%E3%83%AB%E3%83%BC%E3%83%AB/%E3%83%AB%E3%83%BC%E3%83%ABv2)、
[利用規約](https://www.raagu.info/wiki/%E3%83%AB%E3%83%BC%E3%83%AB/%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84)、
管理者からの案内を優先してください。

## 3. 対応環境

- Minecraft Java Edition 26.2
- Java 25
- Fabric Loader 0.19.3以上
- Minecraft 26.2用Fabric API 0.161.0以上
- MOD ID: `freecam_for_ys`
- 対応バージョン: `1.0.3+mc26.2`

## 4. ダウンロード

参加者が使うのは、**MOD本体のJAR 1個だけ**です。

**[最新版のFreecam for YSをダウンロード](https://github.com/Ra-guyt/Freecam_for_YS/releases/latest)**

開いたページの `Assets` から、**「参加者はこちら：MOD本体」** と案内されている
`.jar` ファイルを1個だけ選んでください。

GitHubが自動で表示する `Source code (zip)` と `Source code (tar.gz)` はソースコードであり、MOD本体ではありません。
ダウンロードしても`mods`フォルダーへは入れないでください。

## 5. 導入方法

1. Minecraft 26.2用のFabric LoaderとFabric APIを用意します。
2. `mods`フォルダーに通常版Freecam、LegacyFreecam、ほかのFreecamがある場合は取り除きます。
3. ダウンロードした `freecam_for_ys-fabric-1.0.3+mc26.2.jar` を`mods`フォルダーへ入れます。
4. Minecraftを起動します。
5. `F4`を押し、画面右下にYSロゴと `Freecam for YS` が表示されれば導入完了です。

Cloth ConfigはJARに入っています。Mod Menuは任意です。

## 6. 操作方法

| 操作 | 既定キー |
| --- | --- |
| Freecamの開始・終了 | `F4` |
| Tripod #1〜#9への切り替え | `F4` + `1`〜`9` |
| Tripodのリセット | 未割当（設定後、キー + `1`〜`9`） |
| 設定画面 | 未割当 |

## 7. 主な技術的制限

- カメラは実プレイヤーの現在地から3次元距離で最大50ブロックまで
- 通常移動、Tripod、保存地点、`moveToEntity`、`moveToPosition`に同じ制限を適用
- 未読込チャンク、異常な座標、ブロックに重なる移動先を拒否
- 攻撃、採掘、設置、アイテム使用、ブロック・エンティティ操作、スポイトを遮断
- Freecam中は実プレイヤーの移動入力を停止
- 被ダメージ、死亡、リスポーン、ディメンション移動、切断時に終了
- 通常版FreecamとLegacyFreecamは同時導入不可
- 使用中はYSロゴと `Freecam for YS` を固定表示。F1でHUDを消しても、F2スクリーンショットにも残る

Full Brightnessは、YSサーバーで暗視MODが許可されているため残しています。
詳しい設計と限界は [docs/SAFETY.md](docs/SAFETY.md)、確認範囲は
[docs/VERIFICATION.md](docs/VERIFICATION.md) を参照してください。

## 8. 不具合・脆弱性報告

通常の不具合はGitHub Issueへ、必要な部分だけを個人情報を消して投稿してください。
脆弱性、悪用手順、サーバー情報、未公開ログは公開Issueへ書かず、
[SECURITY.md](SECURITY.md) の案内に従って非公開で連絡してください。

本番サーバーや第三者の環境で、負荷試験、DoS、スキャン、無断検証を行ってはいけません。

## 9. ライセンス・上流

- 上流: MinecraftFreecam/Freecam `v1.3.5+mc1.21.10`
- 基準コミット: `770ad08a36ed76abe57f6c5733ba212105898e15`
- ソースコード: MIT License（[LICENSE](LICENSE)）
- 上流帰属とロゴ資産の扱い: [NOTICE.md](NOTICE.md)
- 開発参加方法: [CONTRIBUTING.md](CONTRIBUTING.md)

Freecam (Fair Play) / LegacyFreecam のソースコードは使用していません。
