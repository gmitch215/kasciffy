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

// set timeout (5m)
config.set({
    browserNoActivityTimeout: 300000,
    browserDisconnectTimeout: 300000,
    processKillTimeout: 300000,
    client: {
        mocha: {
            timeout: 300000
        }
    }
})

// include test resources
config.files.push(
    { pattern: "**/*.png", included: false, served: true, watched: false },
    { pattern: "**/*.gif", included: false, served: true, watched: false },
);