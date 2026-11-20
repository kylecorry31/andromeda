package com.kylecorry.andromeda.pickers.material

import android.os.Bundle
import android.os.Parcel
import com.google.android.material.datepicker.DayViewDecorator
import org.junit.Assert.assertNotNull
import org.junit.Test

internal class AndromedaDayViewDecoratorTest {

    @Test
    fun materialDecoratorSurvivesParcelRoundTrip() {
        val bundle = Bundle().apply {
            putParcelable("decorator", AndromedaDayViewDecorator().toMaterialDayViewDecorator())
        }

        val parcel = Parcel.obtain()
        try {
            parcel.writeBundle(bundle)
            parcel.setDataPosition(0)
            val restored = parcel.readBundle(javaClass.classLoader)!!
            @Suppress("DEPRECATION")
            val decorator = restored.getParcelable<DayViewDecorator>("decorator")
            assertNotNull(decorator)
        } finally {
            parcel.recycle()
        }
    }
}
