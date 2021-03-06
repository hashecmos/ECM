//
// Dynamsoft JavaScript Library for Basic Initiation of Dynamic Web TWAIN
// More info on DWT: http://www.dynamsoft.com/Products/WebTWAIN_Overview.aspx
//
// Copyright 2018, Dynamsoft Corporation 
// Author: Dynamsoft Team
// Version: 13.4
//
/// <reference path="dynamsoft.webtwain.initiate.js" />
var Dynamsoft = Dynamsoft || { WebTwainEnv: {} };

Dynamsoft.WebTwainEnv.AutoLoad = true;
///
Dynamsoft.WebTwainEnv.Containers = [{ContainerId:'dwtcontrolContainer', Width:270, Height:350}];

/// If you need to use multiple keys on the same server, you can combine keys and write like this 
/// Dynamsoft.WebTwainEnv.ProductKey = 'key1;key2;key3';
Dynamsoft.WebTwainEnv.ProductKey = 'AD8EA8051253CC96D7A61C66E3434417E2D00D03EB1131158BB90031CC31DA8B37CB3628F6EFBEDE45C340B30FAD08B63CDF63B640EB56B3FB7EE9FE93719DF64BEB9032DEC34C66B4302BFCB1F206102EACF4EE637E88955EC67CA59BA9E23E7EE7DA6F35AA9A5B6618EFDCBAA93929350EE7635DEC0C2D322F3A9C6CC00DC68F0D5F70E1324109CA1F538F4913DD014FF1644B4606CFB818AE060A3BF12FFB4B32A02BCFCC3526ECE7073A70582DE6;t00886QAAAD+lVuLcAFPVmhIAMgVcrqy98gcXwo1s/gnShipfMR869BOlvlRSZJOGtlfoa8fF+V6+P9VF3qJZdn5w/bPOzviST+M/zNFLDjKNmhHNwqQbxb4tOg==';
///
Dynamsoft.WebTwainEnv.Trial = true;
///
Dynamsoft.WebTwainEnv.ActiveXInstallWithCAB = false;
///
Dynamsoft.WebTwainEnv.IfUpdateService = false;


(function(){
    var p = document.location.protocol;
    if (p !== 'https:' && p !== 'http:')
		Dynamsoft.WebTwainEnv.ResourcesPath = 'https://demo.dynamsoft.com/DWT/Resources';
})();


/// All callbacks are defined in the dynamsoft.webtwain.install.js file, you can customize them.

// Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', function(){
// 		// webtwain has been inited
// });

