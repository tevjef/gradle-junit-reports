![Build Status](https://travis-ci.org/tevjef/gradle-junit-reports.svg?branch=master)
![Version](https://jitpack.io/v/tevjef/gradle-junit-reports.svg)

![Junit Reports Gradle Plugin](art/logo.png)

# Overview

// TODO 

# How to

Add the JitPack repository and apply these two plugins in your top level build file.

```groovy
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath "TODO"
  }
}

apply plugin: 'TODO'
```

# Local Setup

The repo has an `app` module to test the plugin locally. To do so, run the following command to build the plugin and upload it to the local repository located at the root of the project under `repo`.

```bash
$> make plugin
```

You will need to execute the above command each time you make changes to it in order to get the latest changes applied to the application.
 
## License

    MIT License
    
    Copyright (c) 2019 Tevin Jeffrey
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
