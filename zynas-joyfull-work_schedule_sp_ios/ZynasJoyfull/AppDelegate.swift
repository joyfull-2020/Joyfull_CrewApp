//
//  AppDelegate.swift
//  ZynasJoyfull
//
//  Created by ka on 8/16/18.
//  Copyright © 2018 aris. All rights reserved.
//

import UIKit
import SlideMenuControllerSwift
import Firebase
import UserNotifications
import Alamofire

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var redirecrUrl: String?
    var afNetworkManager: SessionManager!

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
        registerForRemoteNotifications(application)
        SVProgressHUD.setDefaultStyle(SVProgressHUDStyle.dark)
        
        self.window = UIWindow(frame: UIScreen.main.bounds)
        SlideMenuOptions.contentViewScale = 1.0
        SlideMenuOptions.hideStatusBar = false
        if ZynasProvider.getLoginState() {
            setUpMainScreen()
        } else {
            setUpLoginScreen()
        }
        
        print("setup flow done ===> ")
        
        initNetwordLib()
        return true
    }
    
    func initNetwordLib() {
        let httpConfig = URLSessionConfiguration.default
        httpConfig.httpCookieAcceptPolicy = .always
        httpConfig.httpShouldSetCookies = true
        httpConfig.httpCookieStorage = HTTPCookieStorage.shared
        httpConfig.timeoutIntervalForRequest = 30
        
        afNetworkManager = Alamofire.SessionManager(configuration: httpConfig)
    }
    
    func setUpMainScreen() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let shiftViewController = storyboard.instantiateViewController(withIdentifier: "shiftviewcontroller") as! ShiftViewController
        let leftViewController = storyboard.instantiateViewController(withIdentifier: "leftviewcontroller") as! LeftViewController
        leftViewController.shiftViewController = shiftViewController
        
        let slideMenuController = ContainerViewController(mainViewController: shiftViewController, leftMenuViewController: leftViewController)
        slideMenuController.automaticallyAdjustsScrollViewInsets = true
        self.window?.rootViewController = slideMenuController
        self.window?.makeKeyAndVisible()
    }
    
    func setUpLoginScreen() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let loginViewController = storyboard.instantiateViewController(withIdentifier: "loginViewController") as! LoginViewController
        self.window?.rootViewController = loginViewController
        self.window?.makeKeyAndVisible()
    }
    
    func registerForRemoteNotifications(_ application: UIApplication) {
        UNUserNotificationCenter.current().delegate = self
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization( options: authOptions,completionHandler: {_, _ in })
        application.registerForRemoteNotifications()
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    
    // Receive displayed notifications for iOS 10 devices.
    func userNotificationCenter(_: UNUserNotificationCenter, willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        print(userInfo)
        redirecrUrl = userInfo["contents"] as? String
        completionHandler([.alert, .sound])
        print("willPresent has been called")
    }
    
    func userNotificationCenter(_: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        redirecrUrl = userInfo["contents"] as? String
        completionHandler()
        print("didReceive has been called")
    }
    
}

extension AppDelegate: MessagingDelegate {
    func updateToken(_ token: String) {
        print("Send token to server: \(token)")

    }
    
    func updateTokenIfNeeded() {
        let prefs = UserDefaults.standard
        if prefs.value(forKey: "fcmtoken") != nil {
            let token = prefs.value(forKey: "fcmtoken") as! String
            let isUpdate = prefs.value(forKey: "fcmtoken_update") as! Bool
            if !isUpdate {
                updateToken(token)
            }
        }
    }
    
    func receiveToken(_ fcmToken: String) {
        print("receive: \(fcmToken)")
        
        updateToken(fcmToken)
    }
    
    func messaging(_: Messaging, didReceiveRegistrationToken fcmToken: String) {
        receiveToken(fcmToken)
    }

    func messaging(_: Messaging, didReceive remoteMessage: MessagingRemoteMessage) {
        print("Received data message: \(remoteMessage.appData)")
    }

}
