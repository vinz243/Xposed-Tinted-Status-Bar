package com.vinz243.materialstatusbar.hooks;

import android.app.AndroidAppHelper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vinz243.materialstatusbar.ColourChangerMod;
import com.vinz243.materialstatusbar.SettingsHelper;
import com.vinz243.materialstatusbar.SettingsHelper.Tint;
import com.vinz243.materialstatusbar.StatusBarTintApi;
import com.vinz243.materialstatusbar.Utils;
import com.vinz243.materialstatusbar.drawables.IgnoredColorDrawable;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class WindowDecorActionBarHooks {
	private SettingsHelper mSettingsHelper;

	public WindowDecorActionBarHooks(SettingsHelper settingsHelper, XC_LoadPackage.LoadPackageParam lpparam) {
		mSettingsHelper = settingsHelper;

		try {
			Class<?> WindowDecorActionBar = findClass("android.support.v7.internal.app.WindowDecorActionBar", lpparam.classLoader);
			findAndHookMethod(WindowDecorActionBar, "setBackgroundDrawable", Drawable.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					FrameLayout actionBar = (FrameLayout) getObjectField(param.thisObject, "mContainerView");
					String packageName = actionBar.getContext().getPackageName();
					if (!AndroidAppHelper.currentPackageName().equals(packageName))
						return;

					if (!mSettingsHelper.isEnabled(packageName, null))
						return;

					Drawable drawable = (Drawable) param.args[0];
					if (drawable != null) {
						if (drawable instanceof IgnoredColorDrawable) {
							return;
						}
						int color = Utils.getMainColorFromActionBarDrawable(drawable);
						if (mSettingsHelper.shouldReverseTintAbColor(packageName) && actionBar.getVisibility() == View.VISIBLE)
							actionBar.setBackgroundDrawable(new IgnoredColorDrawable(color));
						int defaultNormal = mSettingsHelper.getDefaultTint(Tint.ICON);
						int invertedIconTint = mSettingsHelper.getDefaultTint(Tint.ICON_INVERTED);
						ColourChangerMod.sendColorChangeIntent(color, Utils.getIconColorForColor(color, defaultNormal,
								invertedIconTint, mSettingsHelper.getHsvMax()), actionBar.getContext());
					}
				}
			});

			findAndHookMethod(WindowDecorActionBar, "hide", new XC_MethodHook() {
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					FrameLayout actionBar = (FrameLayout) getObjectField(param.thisObject, "mContainerView");
					String packageName = actionBar.getContext().getPackageName();
					if (!AndroidAppHelper.currentPackageName().equals(packageName))
						return;

					if (!mSettingsHelper.isEnabled(packageName, null))
						return;

					if (!mSettingsHelper.shouldReactToActionBar(packageName, null))
						return;

					Intent intent = new Intent(StatusBarTintApi.INTENT_CHANGE_COLOR_NAME);

					int statusBarTint = mSettingsHelper.getDefaultTint(Tint.STATUS_BAR);
					int defaultNormal = mSettingsHelper.getDefaultTint(Tint.ICON);
					int invertedIconTint = mSettingsHelper.getDefaultTint(Tint.ICON_INVERTED);

					if (!intent.hasExtra(StatusBarTintApi.KEY_STATUS_BAR_ICON_TINT))
						intent.putExtra(StatusBarTintApi.KEY_STATUS_BAR_ICON_TINT,
								mSettingsHelper.getDefaultTint(Tint.ICON));

					if (mSettingsHelper.shouldReactToActionBarVisibility())
						ColourChangerMod.sendColorChangeIntent(statusBarTint, Utils.getIconColorForColor(statusBarTint, defaultNormal,
								invertedIconTint, mSettingsHelper.getHsvMax()), actionBar.getContext());
				}

				;
			});

			findAndHookMethod(WindowDecorActionBar, "show", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					FrameLayout actionBar = (FrameLayout) getObjectField(param.thisObject, "mContainerView");
					String packageName = actionBar.getContext().getPackageName();
					if (!AndroidAppHelper.currentPackageName().equals(packageName))
						return;

					if (!mSettingsHelper.isEnabled(packageName, null))
						return;

					if (!mSettingsHelper.shouldReactToActionBar(packageName, null))
						return;

					Object actionBarContainer = actionBar;
					// Object actionBarContainer = getObjectField(actionBar, "mContainerView");
					int actionBarTextColor = -2;
					try {
						TextView mTitleView = (TextView) getObjectField(
								getObjectField(actionBarContainer, "mActionBarView"), "mTitleView");
						if (mTitleView != null) {
							if (mTitleView.getVisibility() == View.VISIBLE) {
								actionBarTextColor = mTitleView.getCurrentTextColor();
							}
						}
					} catch (Throwable t) {

					}
					Drawable drawable = (Drawable) getObjectField(actionBarContainer, "mBackground");
					if (drawable == null)
						return;

					int color = Utils.getMainColorFromActionBarDrawable(drawable);
					if (mSettingsHelper.shouldReverseTintAbColor(packageName))
						actionBar.setBackgroundDrawable(new IgnoredColorDrawable(color));
					int defaultNormal = mSettingsHelper.getDefaultTint(Tint.ICON);
					int invertedIconTint = mSettingsHelper.getDefaultTint(Tint.ICON_INVERTED);
					int iconTint;

					if (actionBarTextColor != -2) {
						iconTint = actionBarTextColor;
					} else {
						iconTint = Utils.getIconColorForColor(color, defaultNormal,
								invertedIconTint, mSettingsHelper.getHsvMax());
					}

					if (mSettingsHelper.shouldReactToActionBarVisibility())
						ColourChangerMod.sendColorChangeIntent(color, iconTint, actionBar.getContext());
				}
			});
		} catch (ClassNotFoundError e) {
		} catch (NoSuchMethodError e) {
		}
	}
}
