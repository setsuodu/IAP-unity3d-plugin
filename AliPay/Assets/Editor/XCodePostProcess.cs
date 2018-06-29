using System.IO;
using UnityEditor;
using UnityEditor.iOS.Xcode;
using UnityEditor.Callbacks;

public class XCodePostProcess
{
	[PostProcessBuild]
	static void OnPostprocessBuild(BuildTarget buildTarget, string path)
	{
		if(buildTarget == BuildTarget.iOS)
		{
			ModifyProj(path);
			SetPlist(path);
		}
	}

    // 设置Xcode配置
	public static void ModifyProj(string path)
	{
		string projPath = PBXProject.GetPBXProjectPath(path);
		PBXProject pbxProj = new PBXProject();
        pbxProj.ReadFromString(File.ReadAllText(projPath));

		// 配置目标TARGETS
		string targetGuid = pbxProj.TargetGuidByName("Unity-iPhone");

        // 关闭bitcode
		//pbxProj.SetBuildProperty(targetGuid, "ENABLE_BITCODE", "false");

		// 添加头文件搜索路径
		pbxProj.AddBuildProperty(targetGuid, "HEADER_SEARCH_PATHS", "$(SRCROOT)/Libraries/Plugins/iOS");

        // 添加.tbd
        pbxProj.AddFileToBuild(targetGuid, pbxProj.AddFile("usr/lib/libz.tbd", "Frameworks/libz.tbd", PBXSourceTree.Sdk));
        pbxProj.AddFileToBuild(targetGuid, pbxProj.AddFile("usr/lib/libc++.tbd", "Frameworks/libc++.tbd", PBXSourceTree.Sdk));

		// 设置TeamID
        //...

		File.WriteAllText(projPath, pbxProj.WriteToString());
	}

    // 设置Plist
	static void SetPlist(string path)
	{
		string plistPath = path + "/Info.plist";
		PlistDocument plist = new PlistDocument ();
		plist.ReadFromString (File.ReadAllText (plistPath));

		// Information Property List
        PlistElementDict plistDict = plist.root;
		//plistDict.SetString("", ""); //示例

        // 支付宝view回调目标
		var urltypes = plistDict.CreateArray("CFBundleURLTypes");
        var item0 = urltypes.AddDict();
        var urlschemes = item0.CreateArray("URL Schemes");
        urlschemes.AddString("app_name"); //设置自己的app_name
      
        File.WriteAllText(plistPath, plist.WriteToString());
	}
}
