//
//  LeftItemViewCell.swift
//  ZynasJoyfull
//
//  Created by ka on 8/20/18.
//  Copyright © 2018 aris. All rights reserved.
//

import UIKit

class LeftItemViewCell: UITableViewCell {

    @IBOutlet weak var ibTitleLabel: UILabel!
    @IBOutlet weak var ibConstaintTitleLeading: NSLayoutConstraint!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
