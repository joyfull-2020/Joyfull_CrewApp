//
//  ZynasProvider.swift
//  ZynasJoyfull
//
//  Created by ka on 8/23/18.
//  Copyright © 2018 aris. All rights reserved.
//

import Foundation

class ZynasProvider {
    
    class func saveLoginState(val: Bool) {
        UserDefaults.standard.set(val, forKey: "login_state")
    }
    
    class func getLoginState() -> Bool {
        return UserDefaults.standard.bool(forKey: "login_state")
    }
    
    class func clearToken() {
        UserDefaults.standard.set("", forKey: "token_storage")
    }
    
    class func getStorageUUID() -> String {
        guard let uuidGen = UserDefaults.standard.string(forKey: "uuidStorage") else {
            let uuid = UUID.init().uuidString
            UserDefaults.standard.set(uuid, forKey: "uuidStorage")
            return uuid
        }
        return uuidGen
    }
    
    class func setStaffCode(userId: String) {
        UserDefaults.standard.set(userId, forKey: "staffCode")
    }
    
    class func getStaffCode() -> String {
        guard let userId =  UserDefaults.standard.string(forKey: "staffCode") else {
            return ""
        }
        return userId
    }
    
    class func setStaffName(name: String) {
        UserDefaults.standard.set(name, forKey: "staffName")
    }
    
    class func getStaffName() -> String {
        guard let staffName =  UserDefaults.standard.string(forKey: "staffName") else {
            return ""
        }
        return staffName
    }
    
    class func saveCookies() {
        guard let cookies = HTTPCookieStorage.shared.cookies(for: URL(string: "\(URL_API_BASE)/")!) else {
            return
        }
        let array = cookies.compactMap { (cookie) -> [HTTPCookiePropertyKey: Any]? in
            cookie.properties
        }
        UserDefaults.standard.set(array, forKey: "cookies")
        UserDefaults.standard.synchronize()
    }
    
    class func loadCookies() {
        guard let cookies = UserDefaults.standard.value(forKey: "cookies") as? [[HTTPCookiePropertyKey: Any]] else {
            return
        }
        var cookiesForUrl = [HTTPCookie]()
        cookies.forEach { (cookie) in
            guard let cookie = HTTPCookie.init(properties: cookie) else {
                return
            }
            cookiesForUrl.append(cookie)
            // HTTPCookieStorage.shared.setCookie(cookie)
        }
        HTTPCookieStorage.shared.setCookies(cookiesForUrl, for:  URL(string: "\(URL_API_BASE)/")!, mainDocumentURL: nil)
    }
    
    class func loadCookiesString() -> String {

        guard let cookies = HTTPCookieStorage.shared.cookies(for:  URL(string: "\(URL_API_BASE)/")!) else {
            return ""
        }
        
        var resultString = ""
        cookies.forEach { (cookie) in
            if resultString == "" {
                resultString = ("\(cookie.name)=\(cookie.value)")
            } else{
                resultString = ("\(resultString);\(cookie.name)=\(cookie.value)")
            }
        }
        return resultString
    }

    class func requestWithUrl(url: URL) -> URLRequest {
        var urlRequest = URLRequest.init(url: url)
        urlRequest.setValue(loadCookiesString(), forHTTPHeaderField: "Cookie" )
        return urlRequest
    }
    
    class func removeCookies() {
        if let cookies = HTTPCookieStorage.shared.cookies(for: URL(string: "\(URL_API_BASE)/")!) {
            for item in cookies {
                HTTPCookieStorage.shared.deleteCookie(item)
            }
        }
        
    }
    
    
    class func saveUserName(username: String) {
        UserDefaults.standard.set(username, forKey: "userName")
    }
    
    class func getUserName() -> String {
        guard let userName = UserDefaults.standard.string(forKey: "userName") else {
            return ""
        }
        return userName
    }
    
    class func savePassword(password: String) {
        UserDefaults.standard.set(password, forKey: "password")
    }
    
    class func getPassword() -> String {
        guard let pass = UserDefaults.standard.string(forKey: "password") else {
            return ""
        }
        return pass
    }
    
    class func getScript() -> String {
        if let filepath = Bundle.main.path(forResource: "script", ofType: "js") {
            do {
                return try String(contentsOfFile: filepath)
            } catch {
                print(error)
            }
        } else {
            print("script.js not found!")
        }
        return ""
    }
    
}
