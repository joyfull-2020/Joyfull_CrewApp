//
//  ViewController.swift
//  ZynasJoyfull
//
//  Created by ka on 8/16/18.
//  Copyright © 2018 aris. All rights reserved.
//

import UIKit

class LoginViewController: UIViewController, UITextFieldDelegate {

    @IBOutlet weak var ibUserNameTf: SkyFloatingLabelTextField!
    @IBOutlet weak var ibPasswordTf: SkyFloatingLabelTextField!
    
    var presenter: LoginPresenter!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        ibUserNameTf.delegate = self
        ibPasswordTf.delegate = self
        presenter = LoginPresenter()
        presenter.delegate = self
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }

    @IBAction func loginAction(_ sender: Any) {
        
        guard ibUserNameTf.text != nil && ibUserNameTf.text!.trimmingCharacters(in: CharacterSet.whitespaces) != "" else {
            let alert = UIAlertController(title: nil, message: "入力してください", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { action in
            }))
            self.present(alert, animated: true, completion: nil)
            return
        }
        
        guard ibPasswordTf.text != nil && ibPasswordTf.text!.trimmingCharacters(in: CharacterSet.whitespaces) != "" else {
            let alert = UIAlertController(title: nil, message: "入力してください", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { action in
            }))
            self.present(alert, animated: true, completion: nil)
            return
        }
        
        SVProgressHUD.show()
        presenter.authen(username: ibUserNameTf.text!.trimmingCharacters(in: CharacterSet.whitespaces), password: ibPasswordTf.text!.trimmingCharacters(in: CharacterSet.whitespaces))
    }
    
}

extension LoginViewController: LoginProtocol {
    
    func loginComplete(state: Bool) {
        ZynasProvider.saveLoginState(val: state)
        SVProgressHUD.dismiss()
        if state {
            (UIApplication.shared.delegate as! AppDelegate).setUpMainScreen()
        } else {
            let alert = UIAlertController(title: nil, message: "ログインに失敗しました", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { action in
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }

    func loginComplete(state: Bool, message: String) {
        ZynasProvider.saveLoginState(val: state)
        SVProgressHUD.dismiss()
        if state {
            (UIApplication.shared.delegate as! AppDelegate).setUpMainScreen()
        } else {
            let alert = UIAlertController(title: nil, message: message, preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { action in
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
}

