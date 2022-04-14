package pl.birski.falldetector.presentation.listener

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

interface NavigateInterface {

    fun navigateToFragment(fragment: Fragment): FragmentTransaction
}
