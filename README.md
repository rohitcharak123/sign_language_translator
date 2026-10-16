<div align="center">

<h1>SignBridge</h1>h1>
<p><b>Breaking Communication Barriers with AI</b>b></p>p>

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

[![Stars](https://img.shields.io/github/stars/rohitsingh-dev-85/sign_language_translator?style=social)](https://github.com/rohitsingh-dev-85/sign_language_translator/stargazers)
[![Forks](https://img.shields.io/github/forks/rohitsingh-dev-85/sign_language_translator?style=social)](https://github.com/rohitsingh-dev-85/sign_language_translator/network/members)
[![Issues](https://img.shields.io/github/issues/rohitsingh-dev-85/sign_language_translator?color=red)](https://github.com/rohitsingh-dev-85/sign_language_translator/issues)

> **An AI-powered Android application that enables real-time communication between deaf and hearing individuals using sign language recognition and 3D avatar animation.**
>
> </div>

---

## Screenshots

<div align="center">

  | Home Screen | Translator | 3D Avatar |
  |:-----------:|:----------:|:---------:|
  | Coming Soon | Coming Soon | Coming Soon |

  </div>

  ---

  ## Features

  - **Real-Time Sign Language Recognition** -- Detects hand gestures using on-device AI vision
  - - **Multi-Language Support** -- Supports ASL (American), ISL (Indian), BSL (British) sign languages
    - - **3D Avatar Animation** -- Converts text to sign language using animated 3D avatars
      - - **Offline Support** -- Works completely on-device, no internet required
        - - **Two-Way Communication** -- Sign to Text and Text to Sign translation
          - - **Privacy First** -- No data leaves your device
            - - **Low Latency** -- Real-time processing under 100ms
             
              - ---

              ## Tech Stack

              | Layer | Technology |
              |-------|-----------|
              | **Language** | Kotlin |
              | **UI Framework** | Jetpack Compose |
              | **AI / ML** | On-device Vision Analysis (MediaPipe / ML Kit) |
              | **3D Rendering** | Custom 3D Avatar Engine |
              | **Build System** | Gradle (Kotlin DSL) |
              | **Min SDK** | Android 8.0 (API 26) |
              | **Target SDK** | Android 14 (API 34) |

              ---
              ## Project Structure

              sign_language_translator/
                app/
                  src/
                    main/
                      java/          # Kotlin source files
                      res/           # UI resources
                      AndroidManifest.xml
                    test/            # Unit tests
                  build.gradle.kts
                  proguard-rules.pro
                gradle/
                  wrapper/
                  libs.versions.toml # Version catalog
                build.gradle.kts
                settings.gradle.kts
                gradle.properties
                .env.example         # Environment variables template

              ---

              ## Getting Started

              ### Prerequisites

              - Android Studio **Hedgehog** (2023.1.1) or newer
              - - JDK **17** or higher
                - - Android SDK **API 26+**
                  - - Kotlin **1.9+**
                   
                    - ### Installation
                   
                    - 1. **Clone the repository**
                      2.    ```bash
                               git clone https://github.com/rohitsingh-dev-85/sign_language_translator.git
                               cd sign_language_translator
                               ```

                            2. **Set up environment variables**
                            3.    ```bash
                                     cp .env.example .env
                                     # Fill in your API keys in .env file
                                     ```

                                  3. **Open in Android Studio**
                                  4.    ```
                                           File -> Open -> Select the project folder
                                           ```

                                        4. **Build and Run**
                                        5.    ```bash
                                                 ./gradlew assembleDebug
                                                 # Or simply press Run in Android Studio
                                                 ```

                                              ---

                                          ## Supported Sign Languages

                                    | Sign Language | Code | Status |
                              |---------------|------|--------|
                              | American Sign Language | ASL | Supported |
                              | Indian Sign Language | ISL | Supported |
                              | British Sign Language | BSL | Supported |
                              | Australian Sign Language | Auslan | Coming Soon |
                              | French Sign Language | LSF | Coming Soon |

                              ---

                        ## How It Works

                      [Camera Input]
                           |
                           v
                      [Hand Landmark Detection] ---- MediaPipe / ML Kit
                           |
                           v
                      [Gesture Classification] ---- On-device AI Model
                           |
                           v
                      [Sign to Text Translation] ---- NLP Processing
                           |
                           v
                      [Text Output / TTS] ---- Real-time Display

                      ----------- Reverse Direction -----------

                      [Text / Speech Input]
                           |
                           v
                      [Text Processing]
                           |
                           v
                      [3D Avatar Animation] ---- Sign Language Output

                      ---

                      ## Model Performance

                      | Metric | Value |
                      |--------|-------|
                      | **Accuracy** | ~92% |
                      | **Inference Time** | < 100ms |
                      | **Model Size** | < 15 MB |
                      | **Supported Gestures** | 500+ |

                      ---

                      ## Roadmap

                      - [x] Basic hand gesture detection
                      - [ ] - [x] ASL, ISL, BSL support
                      - [ ] - [x] 3D Avatar integration
                      - [ ] - [ ] Sentence-level translation
                      - [ ] - [ ] Multi-hand gesture support
                      - [ ] - [ ] Word suggestions & autocomplete
                      - [ ] - [ ] iOS version (Swift/SwiftUI)
                      - [ ] - [ ] Community gesture dataset contributions
                     
                      - [ ] ---
                     
                      - [ ] ## Contributing
                     
                      - [ ] Contributions are what make the open-source community such an amazing place! Any contributions you make are greatly appreciated.
                     
                      - [ ] 1. Fork the Project
                      - [ ] 2. Create your Feature Branch
                      - [ ]    ```bash
                      - [ ]       git checkout -b feature/AmazingFeature
                      - [ ]      ```
                      - [ ]  3. Commit your Changes
                      - [ ]     ```bash
                      - [ ]    git commit -m 'Add some AmazingFeature'
                      - [ ]       ```
                      - [ ]   4. Push to the Branch
                      - [ ]      ```bash
                      - [ ]     git push origin feature/AmazingFeature
                      - [ ]    ```
                      - [ ]    5. Open a Pull Request
                     
                      - [ ]    ---
                     
                      - [ ]    ## License
                     
                      - [ ]    Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for more information.
                     
                      - [ ]    ---
                     
                      - [ ]    ## Author
                     
                      - [ ]    <div align="center">

                      **Rohit Singh**

                      [![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/rohitsingh-dev-85)
                      [![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/rohitsingh)

                      *Full Stack Developer & ML Engineer | Computer Science Student*

                      </div>

                      ---

                      ## Acknowledgements

                      - [MediaPipe](https://mediapipe.dev/) -- Hand landmark detection
                      - - [Jetpack Compose](https://developer.android.com/jetpack/compose) -- Modern Android UI toolkit
                        - - [Android ML Kit](https://developers.google.com/ml-kit) -- On-device machine learning
                          - - The Deaf & Hard of Hearing community -- for the inspiration
                           
                            - ---

                            <div align="center">

                            ### If this project helped you, please give it a star!

                            *"Technology should empower everyone -- including those who communicate differently."*

                            </div>
                            
</p></h1>
