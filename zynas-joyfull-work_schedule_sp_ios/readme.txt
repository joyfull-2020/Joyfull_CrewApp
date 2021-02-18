[Firebase Account]
Id: zynas.joyapp@gmail.com
Pass: Zynas6650

[開発環境設定]

CocoaPodsをインストールしてない場合
・Macでターミナルを立ち上げ以下のコマンドを入力してください

　　>sudo gem update —system
　　>sudo gem install cocoapods -n /usr/local/bin
　　>pod setup

インストール後又はインストール済みの場合

・アプリのプロジェクトの直下のディレクトリをカレントディレクトリにする（Podfileというfileがあるディレクトリです）
・以下のコマンドを入力してください

　　   >pod update

その後、xcodeでZynasJoyfull.xcworkspaceを指定してプロジェクトを開いて下さい
