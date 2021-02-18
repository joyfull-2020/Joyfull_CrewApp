//
//  LoginPresenter.swift
//  ZynasJoyfull
//
//  Created by ka on 8/17/18.
//  Copyright © 2018 aris. All rights reserved.
//

import Foundation
import Alamofire
import Firebase

protocol LoginProtocol {
    func loginComplete(state: Bool)
    func loginComplete(state: Bool, message: String)
}

class LoginPresenter {
    
    var delegate: LoginProtocol!
    var afManager: SessionManager!
    
    func authen(username: String, password: String) {
        afManager = (UIApplication.shared.delegate as! AppDelegate).afNetworkManager
        ZynasProvider.removeCookies()
        if !NetworkReachabilityManager()!.isReachable {
            self.delegate.loginComplete(state: false, message: "ネットワークに接続していません")
            return
        }
        getCsrToken(username, password)
    }

    func getCsrToken(_ username: String, _ password: String) {
        let url = URL(string: "\(URL_API_BASE)\(CSRF)")
        afManager.request(url!, method: .get, parameters: nil) .validate() .response { (response) in
            if response.error != nil {
                self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
                return
            }
            let headers = response.response?.allHeaderFields
            let cookies = HTTPCookie.cookies(withResponseHeaderFields: headers as! [String : String], for: URL(string: "\(URL_API_BASE)/")!)
            self.afManager.session.configuration.httpCookieStorage?.setCookies(cookies, for: URL(string: "\(URL_API_BASE)/")!, mainDocumentURL: nil )
            if response.data != nil {
                let token = String(data: response.data!, encoding: String.Encoding.utf8)
                self.login( username, password, token!)
            } else {
                self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
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
                    self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
                }
                return URLRequest(url: request.url!)
            }
            
            let redirecLocation: String = headerRedirect["Location"] as! String
            let redirectFullLocation =  "\(URL_SERVER_BASE)\(redirecLocation)"
            
            let cookies = HTTPCookie.cookies(withResponseHeaderFields: headerRedirect as! [String : String], for: URL(string: "\(URL_API_BASE)/")!)
            self.afManager.request(URL.init(string: redirectFullLocation)!).response(completionHandler: { (res) in
                if res.error != nil {
                    self.delegate.loginComplete(state: false, message: "ログインに失敗しました")
                    return
                }
                
                if res.response!.statusCode == 200 {
                    ZynasProvider.removeCookies()
                    HTTPCookieStorage.shared.setCookies(cookies, for: URL(string: "\(URL_API_BASE)/")!, mainDocumentURL: nil)
                    ZynasProvider.saveCookies()
                    ZynasProvider.saveUserName(username: username)
                    ZynasProvider.savePassword(password: password)
                    
                    self.getUserInfo()
                } else {
                    self.delegate.loginComplete(state: false, message: "ログインに失敗しました")
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
                self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
                return
            }
            
            if response.response!.statusCode != 200 {
                self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
            }
        }
    }
    
    func getUserInfo() {
        
        let url = URL(string: "\(URL_API_BASE)\(USER_INFO)")
        afManager.request(url!, method: .get, parameters: nil) .validate() .response { (response) in
            if response.error != nil {
                self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
                return
            }
            if response.data != nil {
                //let responseData = String(data: response.data!, encoding: String.Encoding.utf8)
                let responseData: [String: Any] = try! JSONSerialization.jsonObject(with: response.data!, options: []) as! [String : Any]
                print("\(responseData["staffName"]!) ----- \(responseData["staffCd"]!)" )
                
                ZynasProvider.setStaffCode(userId: responseData["staffCd"]! as! String)
                ZynasProvider.setStaffName(name: responseData["staffName"]! as! String)
                self.registerPush()
            } else {
                self.delegate.loginComplete(state: false, message: "認証エラーが発生しました")
            }
        }
        
    }
    
    func registerPush() {
        if Messaging.messaging().fcmToken != nil {
            let url = URL(string: "\(URL_API_BASE)\(CSRF)")
            afManager.request(url!, method: .get, parameters: nil) .validate() .response { (response) in
                if response.error != nil {
                    self.delegate.loginComplete(state: false, message: "通知の登録に失敗しました")
                    return
                }
//                let headers = response.response?.allHeaderFields
//                let cookies = HTTPCookie.cookies(withResponseHeaderFields: headers as! [String : String], for: URL(string: "\(URL_API_BASE)/")!)
//                self.afManager.session.configuration.httpCookieStorage?.setCookies(cookies, for: URL(string: "\(URL_API_BASE)/")!, mainDocumentURL: nil )
                if response.data != nil {
                    let token = String(data: response.data!, encoding: String.Encoding.utf8)
                    self.doRegist( token!)
                }else {
                    self.delegate.loginComplete(state: false, message: "通知の登録に失敗しました")
                }
            }
        }else {
            self.delegate.loginComplete(state: false, message: "通知の登録に失敗しました")
        }
    }
    
    
    func doRegist(_ token: String ) {
        let url = URL.init(string: "\(URL_API_BASE)\(REGIST)")
        
        var parameter = Parameters()
        parameter["uUID"] = ZynasProvider.getStorageUUID()
        parameter["staffCode"] = ZynasProvider.getStaffCode()
        parameter["modelName"] = UIDevice.current.modelName
        parameter["pushToken"] = Messaging.messaging().fcmToken!
        
        let cookies = HTTPCookieStorage.shared.cookies(for: URL(string: "\(URL_API_BASE)/")!)
        var cookiPath = ""
        for item in cookies! {
            if cookiPath == "" {
                cookiPath = cookiPath + "\(item.name)=\(item.value)"
            } else {
                cookiPath = cookiPath + "; \(item.name)=\(item.value)"
            }
        }
        
        let headers: HTTPHeaders = [
            "Content-Type":"application/json; charset=UTF-8",
            "cookie":cookiPath,
            "x-csrf-token": token,
            "Accept":"application/json"
        ]
        
        afManager.request(url!, method: HTTPMethod.post, parameters: parameter, encoding: JSONEncoding.default, headers: headers ).validate().responseJSON { (response) in
            if response.response!.statusCode == 200 {
                self.delegate.loginComplete(state: true)
            } else {
                self.delegate.loginComplete(state: false, message: "通知の登録に失敗しました")
            }
        }
    }
    
}
