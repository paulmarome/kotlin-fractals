package org.jetbrains.demo.kotlinfractals

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseMoveFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledCanvas

data class PixelInfo(val x : Int, val y: Int)

interface AutoResizeCanvasControlProps : RProps {
  var canvasSize : ScreenInfo

  //hack to make shouldComponentUpdate work
  var renderMode : Any?
  var renderImage : (suspend CoroutineScope.(JSFractalImage) -> Unit)?

  var onMouseMove : ((PixelInfo) -> Unit)?
  var onMouseClick: ((PixelInfo) -> Unit)?
}

class AutoResizeCanvasControl : RComponent<AutoResizeCanvasControlProps, RState>() {
  private var job : Job? = null

  override fun shouldComponentUpdate(nextProps: AutoResizeCanvasControlProps, nextState: RState): Boolean {
    if (props.canvasSize != nextProps.canvasSize) return true
    if (props.renderMode != nextProps.renderMode) return true

    return false
  }

  override fun RBuilder.render() {
    styledCanvas {
      //cache it just in case

      props.renderImage?.let { builder ->
        ref {
          if (it != null) {
            job = launch {
              yield()
              builder(fractalImageFromCanvas(it))
            }
          } else {
            job?.let { job ->
              if (job.isActive) {
                println("Cancelling rendering job")
                it.cancel()
              }
            }
          }
        }
      }

      css { +Styles.canvas }

      attrs {
        width = props.canvasSize.width.toString()
        height = props.canvasSize.height.toString()

        onMouseMoveFunction = fireCoordinatesEvent(props.onMouseMove)
        onClickFunction = fireCoordinatesEvent(props.onMouseClick)
      }
    }
  }

  private fun fireCoordinatesEvent(it: ((PixelInfo) -> Unit)?) : (Event) -> Unit {
    return { e: dynamic ->
      it?.invoke(PixelInfo(e.nativeEvent.offsetX, e.nativeEvent.offsetY))
    }
  }
}
