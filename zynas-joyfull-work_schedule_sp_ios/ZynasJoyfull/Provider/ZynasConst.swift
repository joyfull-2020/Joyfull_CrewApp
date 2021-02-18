//
//  ZynasConst.swift
//  ZynasJoyfull
//
//  Created by ka on 8/17/18.
//  Copyright © 2018 aris. All rights reserved.
//

import Foundation

// AWS
//let URL_SERVER_BASE = "http://52.198.238.115"
//let URL_API_BASE = "http://52.198.238.115/work_schedule_mobile_testing"

// 検証（ドメイン指定だとSSLがないのでNG）
//let URL_SERVER_BASE = "http://210.146.51.131"
//let URL_API_BASE = "http://210.146.51.131/work_schedule_mobile_staging"

// 本番
let URL_SERVER_BASE = "https://joy-appli.joyfull.co.jp"
let URL_API_BASE = "https://joy-appli.joyfull.co.jp/work_schedule_mobile_production"

let LOGIN = "/j_spring_security_check"
let CSRF = "/csrf"
let REGIST = "/com/regist_device"
let USER_INFO = "/com/login_info_json"
