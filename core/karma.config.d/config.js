// detect browsers
config.frameworks.push("detectBrowsers");
config.set({
    browsers: [],
    detectBrowsers: {
        enabled: true,
        usePhantomJS: false,
        preferHeadless: true,
        postDetection: function(browsers) {
            browsers = browsers.filter((browser) => browser.includes("Headless"))
            return browsers;
        }
    }
});
config.plugins.push("karma-detect-browsers");

// set timeout (30s)
config.set({
    browserNoActivityTimeout: 30000,
    browserDisconnectTimeout: 30000,
    processKillTimeout: 30000,
    client: {
        mocha: {
            timeout: 30000
        }
    }
})

// include .png files
config.files.push(
    { pattern: "**/*.png", included: false, served: true, watched: false },
);