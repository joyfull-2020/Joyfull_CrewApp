//
//  LeftPresenter.swift
//  ZynasJoyfull
//
//  Created by ka on 8/20/18.
//  Copyright © 2018 aris. All rights reserved.
//

import Foundation

protocol LeftMenuProtocol {
    
    func logoutComplete()
    
}

class LeftPresenter {
    
    var delegate: LeftMenuProtocol?
    
    func logout() {
        delegate?.logoutComplete()
    }
    
}
