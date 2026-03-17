; ============================================================
; PharmDesk - Inno Setup Installer Script (With Bundled JRE)
; ============================================================
; BEFORE BUILDING:
;   1. Run: .\mvnw.cmd package
;   2. Confirm target\PharmDesk.jar exists
;   3. Confirm jre\ folder exists in project root
;   4. Confirm javafx-sdk\javafx-sdk-25.0.2\ exists in project root
;   5. Open this file in Inno Setup Compiler and click Build
; ============================================================

#define AppName      "PharmDesk"
#define AppVersion   "1.0.0"
#define AppPublisher "Afzal"
#define ProjectRoot  "D:\Pharmacy Management\Pharmacy"
#define JavaFXLib    "D:\Pharmacy Management\Pharmacy\javafx-sdk\javafx-sdk-25.0.2\lib"

[Setup]
AppId={{A3F2B1C4-7E8D-4F9A-B2C3-D4E5F6A7B8C9}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL=https://github.com/Afzal-20
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
DisableProgramGroupPage=yes
OutputDir={#ProjectRoot}\installer-output
OutputBaseFilename=PharmDesk_Setup_v{#AppVersion}
SetupIconFile={#ProjectRoot}\src\main\resources\images\logo 1.ico
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64
MinVersion=10.0

[Files]
; 1. The Fat JAR
Source: "{#ProjectRoot}\target\PharmDesk.jar";                  DestDir: "{app}";                      Flags: ignoreversion
; 2. The Application Icon
Source: "{#ProjectRoot}\src\main\resources\images\logo 1.ico";  DestDir: "{app}";                      Flags: ignoreversion
; 3. Default Configuration File (only copied if one does not already exist)
Source: "{#ProjectRoot}\config.properties";                     DestDir: "{commonappdata}\{#AppName}"; Flags: ignoreversion onlyifdoesntexist
; 4. Bundled JRE (no Java install required on target machine)
Source: "{#ProjectRoot}\jre\*";                                 DestDir: "{app}\jre";                  Flags: ignoreversion recursesubdirs createallsubdirs
; 5. JavaFX SDK libs
Source: "{#JavaFXLib}\*";                                       DestDir: "{app}\javafx\lib";           Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autodesktop}\{#AppName}";                              Filename: "{app}\PharmDesk.vbs"; WorkingDir: "{app}"; IconFilename: "{app}\logo 1.ico"
Name: "{autoprograms}\{#AppName}\{#AppName}";                  Filename: "{app}\PharmDesk.vbs"; WorkingDir: "{app}"; IconFilename: "{app}\logo 1.ico"
Name: "{autoprograms}\{#AppName}\Uninstall {#AppName}";        Filename: "{uninstallexe}"

[Registry]
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\Uninstall\{#AppName}"; ValueType: string; ValueName: "DisplayName";    ValueData: "{#AppName}";    Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\Uninstall\{#AppName}"; ValueType: string; ValueName: "DisplayVersion"; ValueData: "{#AppVersion}"
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\Uninstall\{#AppName}"; ValueType: string; ValueName: "Publisher";      ValueData: "{#AppPublisher}"

[Run]
Filename: "{app}\PharmDesk.vbs"; Description: "Launch PharmDesk now"; Flags: postinstall nowait skipifsilent shellexec

[Code]

procedure CreateLauncher();
var
  LauncherPath: string;
  LauncherContent: string;
begin
  LauncherPath := ExpandConstant('{app}\PharmDesk.bat');
  LauncherContent :=
    '@echo off' + #13#10 +
    'cd /d "%~dp0"' + #13#10 +
    'start "" /b "%~dp0jre\bin\javaw.exe"' +
    ' --module-path "%~dp0javafx\lib"' +
    ' --add-modules javafx.controls,javafx.fxml' +
    ' --add-opens java.base/java.lang=ALL-UNNAMED' +
    ' --add-opens java.base/java.io=ALL-UNNAMED' +
    ' --add-opens java.desktop/sun.awt=ALL-UNNAMED' +
    ' -jar "%~dp0PharmDesk.jar"' + #13#10;
  SaveStringToFile(LauncherPath, LauncherContent, False);
end;

procedure CreateVBSWrapper();
var
  VBSPath: string;
  VBSContent: string;
begin
  VBSPath := ExpandConstant('{app}\PharmDesk.vbs');
  VBSContent :=
    'Dim strPath' + #13#10 +
    'strPath = Left(WScript.ScriptFullName, InStrRev(WScript.ScriptFullName, "\"))' + #13#10 +
    'Set WshShell = CreateObject("WScript.Shell")' + #13#10 +
    'WshShell.Run "cmd /c """ & strPath & "PharmDesk.bat""", 0, False' + #13#10;
  SaveStringToFile(VBSPath, VBSContent, False);
end;

procedure CreateProgramDataDirs();
var
  DataDir: string;
begin
  DataDir := ExpandConstant('{commonappdata}\PharmDesk\');
  if not DirExists(DataDir)               then CreateDir(DataDir);
  if not DirExists(DataDir + 'Invoices\') then CreateDir(DataDir + 'Invoices\');
  if not DirExists(DataDir + 'Returns\')  then CreateDir(DataDir + 'Returns\');
  if not DirExists(DataDir + 'backups\')  then CreateDir(DataDir + 'backups\');
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    CreateProgramDataDirs();
    CreateLauncher();
    CreateVBSWrapper();
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
  DataDir: string;
  MsgResult: Integer;
begin
  if CurUninstallStep = usPostUninstall then
  begin
    DataDir := ExpandConstant('{commonappdata}\PharmDesk\');
    if DirExists(DataDir) then
    begin
      MsgResult := MsgBox(
        'Do you want to delete all PharmDesk data?' + #13#10 + #13#10 +
        'This includes:' + #13#10 +
        '  - The database (all sales, inventory, customers)' + #13#10 +
        '  - All saved invoices (PDFs)' + #13#10 +
        '  - All backups' + #13#10 + #13#10 +
        'Location: ' + DataDir + #13#10 + #13#10 +
        'Click YES to delete everything.' + #13#10 +
        'Click NO to keep your data (you can delete it manually later).',
        mbConfirmation, MB_YESNO);
      if MsgResult = IDYES then
        DelTree(DataDir, True, True, True);
    end;
  end;
end;
