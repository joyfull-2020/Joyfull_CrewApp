//
//  ContainerViewController.swift
//  ZynasJoyfull
//
//  Created by ka on 8/17/18.
//  Copyright © 2018 aris. All rights reserved.
//

import Foundation
import UIKit
import SlideMenuControllerSwift

class ContainerViewController : SlideMenuController {

    override func isTagetViewController() -> Bool {
        if let vc = UIApplication.topViewController() {
            if vc is MainViewController ||
                vc is ShiftViewController ||
                vc is ActualViewController {
                return true
            }
        }
        return false
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
//        let statusView = UIView.init(frame: CGRect.zero)
//
//        self.view.insertSubview(statusView, at: 0)
//        statusView.translatesAutoresizingMaskIntoConstraints = false
//        statusView.topAnchor.constraint(equalTo: self.view.topAnchor).isActive = true
//        statusView.leadingAnchor.constraint(equalTo: self.view.leadingAnchor).isActive = true
//        statusView.trailingAnchor.constraint(equalTo: self.view.trailingAnchor).isActive = true
//
//        if #available(iOS 11.0, *) {
//            statusView.bottomAnchor.constraint(equalTo: self.view.safeAreaLayoutGuide.topAnchor).isActive = true
//        } else {
//            statusView.bottomAnchor.constraint(equalTo: self.topLayoutGuide.topAnchor).isActive = true
//        }
//        statusView.backgroundColor = UIColor(hexString: "99ffffff")
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
    }
}
