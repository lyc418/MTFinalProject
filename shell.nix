{ pkgs ? import <nixpkgs> { config.android_sdk.accept_license = true; } }:

let
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    buildToolsVersions = [ "34.0.0" ];
    platformVersions = [ "34" ];
    abiVersions = [ "arm64-v8a" "armeabi-v7a" "x86_64" ];
  };
  androidSdk = androidComposition.androidsdk;
in
pkgs.mkShell {
  buildInputs = with pkgs; [
    androidSdk
    jdk17
    gradle
  ];

  ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
  ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
  GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/34.0.0/aapt2";
}
