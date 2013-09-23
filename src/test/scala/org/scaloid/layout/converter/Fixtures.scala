package org.scaloid.layout.converter

object Fixtures {

    val xml1 =
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:orientation="vertical">

        <Button
          android:id="@+id/startTuner"
          android:layout_width="match_parent"
          android:layout_height="60dp"
          android:onClick="onTunerStart"
          android:text="@string/tuner_on"
          android:textSize="25sp" />

        <LinearLayout
          android:id="@+id/linearLayout2"
          android:layout_width="match_parent"
          android:layout_height="wrap_content">

          <TextView
            android:id="@+id/textView1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="special: \&amp;&lt;&gt;"
            android:textAppearance="?android:attr/textAppearanceMedium">
              <requestFocus/>
          </TextView>

          <Button
            android:id="@+id/minus"
            android:layout_width="0px"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="-"
            android:textSize="30sp" />

          <Button
            android:id="@+id/plus"
            android:layout_width="0px"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="+"
            android:textSize="30sp" />

          <TextView
            android:id="@+id/bps"
            android:layout_width="0px"
            android:layout_height="50dp"
            android:layout_weight="1"
            android:text="@string/bps"
            android:textAppearance="?android:attr/textAppearanceLarge"
            android:textColor="@color/yellow"
            android:textSize="35sp"
            android:gravity="center_vertical|center_horizontal"
            android:layout_gravity="center" />

        </LinearLayout>

        <LinearLayout
          android:id="@+id/linearLayout3"
          android:layout_width="match_parent"
          android:layout_height="wrap_content">

          <TextView
            android:id="@+id/textView2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/timesignature"
            android:textAppearance="?android:attr/textAppearanceMedium" />

          <Spinner
            android:id="@+id/beatspinner"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1" />

        </LinearLayout>

        <LinearLayout
          android:id="@+id/linearLayout4"
          android:layout_width="match_parent"
          android:layout_height="wrap_content">

          <TextView
            android:id="@+id/textView3"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:text="@string/volume"
            android:textAppearance="?android:attr/textAppearanceMedium" />

          <SeekBar
            android:id="@+id/volumebar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:padding="10dp" />

        </LinearLayout>

        <Button
          android:id="@+id/startstop"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:onClick="onStartStopClick"
          android:text="@string/start"
          android:textColor="#6789ab"
          android:textSize="70sp" />

      </LinearLayout>

    val xml2 =
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    android:orientation="vertical" android:layout_width="match_parent"
                    android:layout_height="wrap_content" android:padding="20dip">
        <TextView android:layout_width="match_parent"
                  android:layout_height="wrap_content" android:text="Sign in"
                  android:layout_marginBottom="25dip" android:textSize="24.5sp"/>
        <TextView android:layout_width="match_parent"
                  android:layout_height="wrap_content" android:text="ID"/>
        <EditText android:layout_width="match_parent"
                  android:layout_height="wrap_content" android:id="@+id/userId"/>
        <TextView android:layout_width="match_parent"
                  android:layout_height="wrap_content" android:text="Password"/>
        <EditText android:layout_width="match_parent"
                 android:layout_height="wrap_content" android:id="@+id/password"
                 android:inputType="textPassword"/>
        <Button android:layout_width="match_parent"
                 android:layout_height="wrap_content" android:id="@+id/signin"
                 android:text="Sign in"/>
        <LinearLayout android:orientation="horizontal"
                 android:layout_width="match_parent"
                 android:layout_height="wrap_content">
          <Button android:text="Help" android:id="@+id/help"
                  android:layout_width="match_parent" android:layout_height="wrap_content"/>
          <Button android:text="Sign up" android:id="@+id/signup"
                  android:layout_width="match_parent" android:layout_height="wrap_content"/>
         </LinearLayout>
      </LinearLayout>
}