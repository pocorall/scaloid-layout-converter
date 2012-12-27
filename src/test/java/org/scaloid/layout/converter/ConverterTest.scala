package org.scaloid.layout.converter

import junit.framework.TestCase

class ConverterTest extends TestCase {
  def testConverter {

    val str =
      """

         <?xml version="1.0" encoding="utf-8"?>
        |<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        |	android:layout_width="fill_parent"
        |	android:layout_height="fill_parent"
        |	android:orientation="vertical">
        |
        |	<Button
        |		android:id="@+id/startTuner"
        |		android:layout_width="match_parent"
        |		android:layout_height="60dp"
        |		android:onClick="onTunerStart"
        |		android:text="@string/tuner_on"
        |		android:textSize="25dp" />
        |
        |	<LinearLayout
        |		android:id="@+id/linearLayout2"
        |		android:layout_width="match_parent"
        |		android:layout_height="wrap_content">
        |
        |		<TextView
        |			android:id="@+id/textView1"
        |			android:layout_width="wrap_content"
        |			android:layout_height="wrap_content"
        |			android:text="@string/bps"
        |			android:textAppearance="?android:attr/textAppearanceMedium" />
        |
        |		<Button
        |			android:id="@+id/minus"
        |			android:layout_width="0px"
        |			android:layout_height="wrap_content"
        |			android:layout_weight="1"
        |			android:text="-"
        |			android:textSize="30dp" />
        |
        |		<Button
        |			android:id="@+id/plus"
        |			android:layout_width="0px"
        |			android:layout_height="wrap_content"
        |			android:layout_weight="1"
        |			android:text="+"
        |			android:textSize="30dp" />
        |
        |		<TextView
        |			android:id="@+id/bps"
        |			android:layout_width="0px"
        |			android:layout_height="50dp"
        |			android:layout_weight="1"
        |			android:text="@string/bps"
        |			android:textAppearance="?android:attr/textAppearanceLarge"
        |			android:textColor="@color/yellow"
        |			android:textSize="35dp"
        |			android:gravity="center_vertical|center_horizontal"
        |			android:layout_gravity="center" />
        |
        |	</LinearLayout>
        |
        |	<LinearLayout
        |		android:id="@+id/linearLayout3"
        |		android:layout_width="match_parent"
        |		android:layout_height="wrap_content">
        |
        |		<TextView
        |			android:id="@+id/textView2"
        |			android:layout_width="wrap_content"
        |			android:layout_height="wrap_content"
        |			android:text="@string/timesignature"
        |			android:textAppearance="?android:attr/textAppearanceMedium" />
        |
        |		<Spinner
        |			android:id="@+id/beatspinner"
        |			android:layout_width="wrap_content"
        |			android:layout_height="wrap_content"
        |			android:layout_weight="1" />
        |
        |	</LinearLayout>
        |
        |	<LinearLayout
        |		android:id="@+id/linearLayout4"
        |		android:layout_width="match_parent"
        |		android:layout_height="wrap_content">
        |
        |		<TextView
        |			android:id="@+id/textView3"
        |			android:layout_width="wrap_content"
        |			android:layout_height="match_parent"
        |			android:text="@string/volume"
        |			android:textAppearance="?android:attr/textAppearanceMedium" />
        |
        |		<SeekBar
        |			android:id="@+id/volumebar"
        |			android:layout_width="match_parent"
        |			android:layout_height="wrap_content"
        |			android:layout_weight="1"
        |			android:padding="10dp" />
        |
        |	</LinearLayout>
        |
        |	<Button
        |		android:id="@+id/startstop"
        |		android:layout_width="match_parent"
        |		android:layout_height="match_parent"
        |		android:onClick="onStartStopClick"
        |		android:text="@string/start"
        |		android:textSize="70dp" />
        |
        |
        |</LinearLayout>
      """.stripMargin

    println(Converter(str))
  }
}