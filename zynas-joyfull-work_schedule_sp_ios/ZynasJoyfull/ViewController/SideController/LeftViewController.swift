//
//  LeftViewController.swift
//  ZynasJoyfull
//
//  Created by ka on 8/17/18.
//  Copyright © 2018 aris. All rights reserved.
//

import Foundation
import UIKit

class LeftViewController : UIViewController {
    
    var mainViewController: MainViewController!
    var shiftViewController: ShiftViewController!
    var actualViewController: ActualViewController!
    
    @IBOutlet weak var ibLeftMenuPanel: UIView!
    @IBOutlet weak var ibTableView: UITableView!
    @IBOutlet weak var ibUsernameLabel: UILabel!
    
    let sectionData = ["Joy-シフト",""]
    let itemData = [["休日申込一覧","週間シフト","個人別勤務実績"],["ログアウト"]]
    
    var presenter: LeftPresenter!
    var currentViewControllerIndex = 1
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        presenter = LeftPresenter()
        presenter.delegate = self
        
        //shiftViewController = self.storyboard?.instantiateViewController(withIdentifier: "shiftviewcontroller") as! ShiftViewController
        mainViewController = self.storyboard?.instantiateViewController(withIdentifier: "mainviewcontroller") as? MainViewController
        actualViewController = self.storyboard?.instantiateViewController(withIdentifier: "actualviewcontroller") as?
            ActualViewController
        
        setUpPanelGradient()
        
        ibUsernameLabel.text = ZynasProvider.getStaffName()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    func setUpPanelGradient() {
        let gradient: CAGradientLayer = CAGradientLayer()
        gradient.colors = [UIColor(hexString: "502700").cgColor, UIColor(hexString: "502700").cgColor, UIColor(hexString: "F8B500").cgColor]
        gradient.startPoint = CGPoint(x: 0.0, y: 0.3)
        gradient.endPoint = CGPoint(x: 1.0, y: 0.7)
        gradient.frame = ibLeftMenuPanel.bounds

        ibLeftMenuPanel.layer.insertSublayer(gradient, at: 0)
    }

}

extension LeftViewController: UITableViewDelegate, UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return sectionData.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 3
        default:
            return 1
        }
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        let sectionTitle = sectionData[section]
        if sectionTitle != "" {
            return 56
        } else {
            return 1
        }
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let sectionTitle = sectionData[section]
        if sectionTitle != "" {
            let title = UILabel(frame: CGRect.init(x: 16, y: 0, width: 238, height: 56))
            title.text = "   \(sectionTitle)"
            return title
        } else {
            let dividerView = UIView(frame: CGRect.init(x: 16, y: 0, width: 238, height: 1))
            dividerView.backgroundColor = UIColor.lightGray
            return dividerView
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: LeftItemViewCell = tableView.dequeueReusableCell(withIdentifier: "leftitem", for: indexPath) as! LeftItemViewCell
        let itemTitle = itemData[indexPath.section][indexPath.row]
        
        cell.ibTitleLabel.text = itemTitle
        cell.selectionStyle = .none
        
        switch indexPath.section {
            case 0:
                cell.ibConstaintTitleLeading.constant = 32
                break
            case 1:
                cell.ibConstaintTitleLeading.constant = 16
                break
            default:
                break
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if indexPath.section == 0 {
            switch(indexPath.row) {
                case 0:
                    self.slideMenuController()?.changeMainViewController( mainViewController, close: true)
                    break
                case 1:
                    self.slideMenuController()?.changeMainViewController( shiftViewController, close: true)
                    break
                case 2:
                    self.slideMenuController()?.changeMainViewController(actualViewController, close: true)
                    break
                default:
                    break
            }
            
        } else {
            presenter.logout()
        }
    }
    
}


extension LeftViewController: LeftMenuProtocol {
    
    func logoutComplete() {
        ZynasProvider.saveLoginState(val: false)
        (UIApplication.shared.delegate as! AppDelegate).setUpLoginScreen()
    }
    
}
