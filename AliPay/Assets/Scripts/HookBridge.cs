using System.Runtime.InteropServices;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class HookBridge
{
	[DllImport("__Internal")]
	public static extern string doAPPay(string orderInfo);
}
