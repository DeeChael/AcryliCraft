package io.devbobcorn.acrylic.nativelib;

import java.util.List;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;

public interface User32Lib extends Library {
    
    /* User32 API */
    User32Lib INSTANCE = Native.load("user32", User32Lib.class);
    int INT_SIZE = 4;

	public interface AccentFlags
	{
		// ...
		public static final int DrawLeftBorder = 0x20;

		public static final int DrawTopBorder = 0x40;
		public static final int DrawRightBorder = 0x80;
		public static final int DrawBottomBorder = 0x100;
		public static final int DrawAllBorders = DrawLeftBorder | DrawTopBorder | DrawRightBorder | DrawBottomBorder;

		// ...
	}

    // See https://gist.github.com/Guerra24/429de6cadda9318b030a7d12d0ad58d4
    // WIN10 helpers for compositing (alpha, blur behind)
	public interface AccentState {
		public static final int ACCENT_DISABLED = 0;
		public static final int ACCENT_ENABLE_GRADIENT = 1;
		public static final int ACCENT_ENABLE_TRANSPARENTGRADIENT = 2;
		public static final int ACCENT_ENABLE_BLURBEHIND = 3;
		public static final int ACCENT_ENABLE_ACRYLIC = 4; // YES, available on build 17063
		public static final int ACCENT_INVALID_STATE = 5;
	}

	public static interface WindowCompositionAttribute {
		public static final int WCA_ACCENT_POLICY = 19;
	}

	public class AccentPolicy extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("AccentState", "AccentFlags", "GradientColor",
				"AnimationId");
		public int AccentState;
		public int AccentFlags;
		public int GradientColor;
		public int AnimationId;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	public class WindowCompositionAttributeData extends Structure implements Structure.ByReference {
		public static final List<String> FIELDS = createFieldsOrder("Attribute", "Data", "SizeOfData");
		public int Attribute;
		public Pointer Data;
		public int SizeOfData;

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

    public static void TingeWindow(final long handle, final int gradientColor) {
        HWND targetHWND = new HWND(new Pointer(handle)); // Modify pointer to window

		NativeLibrary user32 = NativeLibrary.getInstance("user32");

		AccentPolicy accent = new AccentPolicy();
		accent.AccentState = AccentState.ACCENT_ENABLE_ACRYLIC;
		accent.GradientColor = gradientColor;
		accent.AccentFlags = AccentFlags.DrawAllBorders;
		accent.write();

		WindowCompositionAttributeData data = new WindowCompositionAttributeData();
		data.Attribute = WindowCompositionAttribute.WCA_ACCENT_POLICY;
		data.SizeOfData = accent.size();
		data.Data = accent.getPointer();

		Function setWindowCompositionAttribute = user32.getFunction("SetWindowCompositionAttribute");
		setWindowCompositionAttribute.invoke(HRESULT.class, new Object[] { targetHWND, data });
    }

}