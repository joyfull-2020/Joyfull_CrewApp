//
//  ShiftViewController.swift
//  ZynasJoyfull
//
//  Created by ka on 8/20/18.
//  Copyright © 2018 aris. All rights reserved.
//

import UIKit
import SlideMenuControllerSwift
import Alamofire
import WebKit

class ShiftViewController: UIViewController, NSURLConnectionDelegate, WKUIDelegate, WKNavigationDelegate {

    @IBOutlet weak var ibWebViewContainer: UIView!
    @IBOutlet weak var ibLoadingIndicator: UIActivityIndicatorView!
    var documentInteract: UIDocumentInteractionController!
    
    var afManager:  SessionManager!
    var wkWebView: WKCookieWebView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        afManager = (UIApplication.shared.delegate as! AppDelegate).afNetworkManager
        
        ZynasProvider.loadCookies()
        initWebView()

        
    }
    
    func initWebView() {
        let preferences = WKPreferences()
        let userController = WKUserContentController()
        preferences.javaScriptEnabled = true
        preferences.javaScriptCanOpenWindowsAutomatically = true
        
        let userScript = WKUserScript(source: "document.getElementById(\"conditionStaffCdButton\").addEventListener('touchstart', function(e) { window.webkit.messageHandlers.notification.postMessage(\"abbbbbcbcbc hu hu hu \");} , false);",
                                      injectionTime: .atDocumentEnd,
                                      forMainFrameOnly: true)
        userController.addUserScript(userScript)
        userController.add(self, name: "notification")
        
        let configuration = WKWebViewConfiguration()
        configuration.userContentController = userController
        configuration.preferences = preferences
        
        
        wkWebView = WKCookieWebView(frame: CGRect.init(x: 0, y: 0, width: UIScreen.main.bounds.size.width, height: ibWebViewContainer.frame.size.height), configuration: configuration)
        wkWebView.uiDelegate = self
        wkWebView.navigationDelegate = self
        
        ibWebViewContainer.addSubview(wkWebView)
        
        wkWebView.translatesAutoresizingMaskIntoConstraints = false
        wkWebView.leadingAnchor.constraint(equalTo: ibWebViewContainer.leadingAnchor).isActive = true
        wkWebView.trailingAnchor.constraint(equalTo: ibWebViewContainer.trailingAnchor).isActive = true
        wkWebView.topAnchor.constraint(equalTo: ibWebViewContainer.topAnchor).isActive = true
        wkWebView.bottomAnchor.constraint(equalTo: ibWebViewContainer.bottomAnchor).isActive = true
        
        // self.addUserScriptToUserContentController(userContent: controller)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        NotificationCenter.default.addObserver(self, selector: #selector(enterForeGround), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
        
        ibLoadingIndicator.startAnimating()
        ibLoadingIndicator.isHidden = false
        
        if let redirectUrlPath = (UIApplication.shared.delegate as! AppDelegate).redirecrUrl {
            _ = wkWebView.load(URLRequest.init(url: URL.init(string: redirectUrlPath)!))
             (UIApplication.shared.delegate as! AppDelegate).redirecrUrl = nil 
        } else {
            let request = URLRequest.init(url: URL.init(string: "\(URL_API_BASE)/weekly_shift")!)
            _ = wkWebView.load(request)
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc func enterForeGround() {
        if ( UIApplication.shared.delegate as! AppDelegate).redirecrUrl != nil {
            ibLoadingIndicator.startAnimating()
            ibLoadingIndicator.isHidden = false
            _ = wkWebView.load(URLRequest.init(url: URL.init(string: (UIApplication.shared.delegate as! AppDelegate).redirecrUrl!)!))
             (UIApplication.shared.delegate as! AppDelegate).redirecrUrl = nil 
        } else {
            
        }
    }

    @IBAction func menuLeftAction(_ sender: Any) {
        self.slideMenuController()?.openLeft()
    }
    
    // wkWebview override function
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationResponse: WKNavigationResponse, decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {
        if (navigationResponse.response is HTTPURLResponse) {
            let response = navigationResponse.response as! HTTPURLResponse
            if response.statusCode != 200  {
                print("re-login")
                self.checkExpireToken()
                decisionHandler(.cancel)
            } else {
                decisionHandler(.allow)
            }
        } else {
            self.checkExpireToken()
            decisionHandler(.cancel)
        }
        
    }

    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        if !NetworkReachabilityManager()!.isReachable {
            self.networkNotAvailable()
        }

        if navigationAction.request.url!.absoluteString.contains("pdf") {
            downloadPDF(fileURL: navigationAction.request.url!)
            decisionHandler(.cancel)
        } else {
            decisionHandler(.allow)
        }
        
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        ibLoadingIndicator.stopAnimating()
        ibLoadingIndicator.isHidden = true
    }
    
    
    
    func webView(_ webView: WKWebView, didReceiveServerRedirectForProvisionalNavigation navigation: WKNavigation!) {
        print("java script has been run ")
    }
    
    func connection(_ connection: NSURLConnection, didFailWithError error: Error) {
        print("failure ---")
    }
    
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        print("failure ---")
    }
    
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        print("Network not found")
    }
    
    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        print("java script has been run ")
    }
    
    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        print("java script has been run ")
    }
    
    func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String, defaultText: String?, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (String?) -> Void) {
        print("java script has been run ")
    }
    
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        print("java script has been run ")
        return nil
    }
    // ****** other functions
    
    func networkNotAvailable() {
        let alert = UIAlertController(title: nil, message: "ネットワークに接続していません", preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { action in
            if self.wkWebView.url == nil {
                let request = URLRequest.init(url: URL.init(string: "\(URL_API_BASE)/weekly_shift")!)
                _ = self.wkWebView.load(request)
            } else {
                self.wkWebView.reload()
            }
        }))
        self.present(alert, animated: true, completion: nil)
        ibLoadingIndicator.stopAnimating()
        ibLoadingIndicator.isHidden = true
    }
    
    func checkExpireToken() {
        
        if !NetworkReachabilityManager()!.isReachable {
            self.networkNotAvailable()
            return
        }
        
        self.getCsrToken(ZynasProvider.getUserName(), ZynasProvider.getPassword())
    }

    
    func downloadPDF(fileURL: URL) {
        let documentsUrl:URL =  FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let sessionConfig = URLSessionConfiguration.default
        let session = URLSession(configuration: sessionConfig)
        
        let request = URLRequest(url:fileURL)
        
        let task = session.downloadTask(with: request) { (tempLocalUrl, response, error) in
            if let tempLocalUrl = tempLocalUrl, error == nil {
                // Success
                guard let statusCode = (response as? HTTPURLResponse)?.statusCode else {
                    return
                }
                
                if statusCode != 200 {
                    self.checkExpireToken()
                    return
                }
                
                let destinationFileUrl = documentsUrl.appendingPathComponent(response!.suggestedFilename!)
                let pathComponent = destinationFileUrl.path
                let fm = FileManager.default
                if fm.fileExists(atPath: pathComponent) {
                    try! fm.removeItem(at: destinationFileUrl)
                }
                
                do {
                    try FileManager.default.copyItem(at: tempLocalUrl, to: destinationFileUrl)
                    
                    DispatchQueue.main.async {
                        self.documentInteract = UIDocumentInteractionController.init(url: destinationFileUrl)
                        self.documentInteract.delegate = self
                        self.documentInteract.presentOptionsMenu(from: self.view.bounds, in: self.view, animated: true)
                    }
                    
                } catch (let writeError) {
                    print("Error creating a file \(destinationFileUrl) : \(writeError)")
                }
                
            } else {
                print("Error took place while downloading a file. Error description: %@", error!.localizedDescription)
            }
        }
        task.resume()
    }
    
    func showExpireTokenDialog() {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: nil, message: "認証エラーが発生しました", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { action in
                ZynasProvider.saveLoginState(val: false)
                (UIApplication.shared.delegate as! AppDelegate).setUpLoginScreen()
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    func getCsrToken(_ username: String, _ password: String) {
        ZynasProvider.removeCookies()
        let url = URL(string: "\(URL_API_BASE)\(CSRF)")
        afManager.request(url!, method: .get, parameters: nil) .validate() .response { (response) in
            if response.error != nil {
                self.showExpireTokenDialog()
                return
            }
            let headers = response.response?.allHeaderFields
            let cookies = HTTPCookie.cookies(withResponseHeaderFields: headers as! [String : String], for: URL(string: "\(URL_API_BASE)/")!)
            self.afManager.session.configuration.httpCookieStorage?.setCookies(cookies, for: URL(string: "\(URL_API_BASE)/")!, mainDocumentURL: nil )
            if response.data != nil {
                let token = String(data: response.data!, encoding: String.Encoding.utf8)
                self.login( username, password, token!)
            } else {
                self.showExpireTokenDialog()
            }
        }
    }
    
    func login(_ username: String,_ password: String,_ token: String) {
        let url = URL(string: "\(URL_API_BASE)\(LOGIN)")
        
        var parameter = Parameters()
        parameter["_csrf"] = token
        parameter["j_username"] = username
        parameter["j_password"] = password
        
        afManager.delegate.taskWillPerformHTTPRedirection = {session, task, response, request in
            let headerRedirect = response.allHeaderFields
            
            if headerRedirect["Set-Cookie"] == nil {
                DispatchQueue.main.async {
                    self.showExpireTokenDialog()
                }
                return URLRequest(url: request.url!)
            }
            
            let redirecLocation: String = headerRedirect["Location"] as! String
            let redirectFullLocation =  "\(URL_SERVER_BASE)\(redirecLocation)"
            
            let cookies = HTTPCookie.cookies(withResponseHeaderFields: headerRedirect as! [String : String], for: URL(string: "\(URL_API_BASE)/")!)
            self.afManager.request(URL.init(string: redirectFullLocation)!).response(completionHandler: { (res) in
                if res.error != nil {
                    self.showExpireTokenDialog()
                    return
                }
                
                if res.response!.statusCode == 200 {
                    HTTPCookieStorage.shared.setCookies(cookies, for: URL(string: "\(URL_API_BASE)/")!, mainDocumentURL: nil)
                    ZynasProvider.saveCookies()
                    self.wkWebView.removeFromSuperview()
                    self.initWebView()
                    _ = self.wkWebView.load(URLRequest.init(url: URL.init(string: "\(URL_API_BASE)/weekly_shift")!))
                } else {
                    self.showExpireTokenDialog()
                }
            })
            return URLRequest(url: request.url!)
        }
        
        let cookies = afManager.session.configuration.httpCookieStorage?.cookies(for: URL(string: "\(URL_API_BASE)/")!)
        var cookiPath = ""
        for item in cookies! {
            if cookiPath == "" {
                cookiPath = cookiPath + "\(item.name)=\(item.value)"
            } else {
                cookiPath = cookiPath + "; \(item.name)=\(item.value)"
            }
        }
        for item in cookies! {
            afManager.session.configuration.httpCookieStorage?.deleteCookie(item)
        }
        
        let headers: HTTPHeaders = [
            "content-type":"application/x-www-form-urlencoded",
            "cookie":cookiPath
        ]
        
        afManager.request(url!, method: .post, parameters: parameter, encoding:  URLEncoding.default, headers: headers).validate().response { (response) in
            if response.error != nil {
                self.showExpireTokenDialog()
                return
            }
            
            if response.response!.statusCode != 200 {
                self.showExpireTokenDialog()
            }
        }
    }
}

extension ShiftViewController : WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if let dict = message.body as? Dictionary<String, AnyObject>, let status = dict["status"] as? Int, let responseUrl = dict["responseURL"] as? String {
            print(status)
            print(responseUrl)
        }
    }
    
}

extension ShiftViewController : SlideMenuControllerDelegate, UIDocumentInteractionControllerDelegate {
    
    func leftWillOpen() {
    }
    
    func leftDidOpen() {
    }
    
    func leftWillClose() {
    }
    
    func leftDidClose() {
    }
    
    func rightWillOpen() {
    }
    
    func rightDidOpen() {
    }
    
    func rightWillClose() {
    }
    
    func rightDidClose() {
    }
    
    func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController {
        return self
    }
}
