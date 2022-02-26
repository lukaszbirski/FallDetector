package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel

@AndroidEntryPoint
class GraphFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                createView()
            }
        }
    }
}

@Composable
fun createView(
    viewModel: GraphViewModel = viewModel()
) {

    viewModel.startMeasurements()

    Column() {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(onClick = {
                viewModel.startService()
            }) {
                Text(
                    text = "Start",
                    style = TextStyle(fontSize = 15.sp)
                )
            }
            Button(onClick = {
                viewModel.stopService()
            }) {
                Text(
                    text = "Stop",
                    style = TextStyle(fontSize = 15.sp)
                )
            }
        }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            createViewFromLayout()
        }
    }
}

@Composable
fun createViewFromLayout() {
    AndroidView(
        factory = {
            LineChart(it)
        }
    ) { lineChart ->
        lineChart.apply {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    createView()
}
