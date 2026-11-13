package ai.moon.celestia.core.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveTaskInfo(
    val id: String,
    val title: String,
    val category: String,
    val startTime: Long = System.currentTimeMillis(),
    val job: Job,
    val onTerminate: (() -> Unit)? = null
)

object CelestiaTaskManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _activeTasksMap = MutableStateFlow<Map<String, ActiveTaskInfo>>(emptyMap())

    val activeTasks: StateFlow<List<ActiveTaskInfo>> = _activeTasksMap
        .map { it.values.toList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val hasActiveTasks: StateFlow<Boolean> = _activeTasksMap
        .map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun registerTask(id: String, title: String, category: String, job: Job, onTerminate: (() -> Unit)? = null) {
        val info = ActiveTaskInfo(id = id, title = title, category = category, job = job, onTerminate = onTerminate)
        val current = _activeTasksMap.value.toMutableMap()
        current[id] = info
        _activeTasksMap.value = current

        job.invokeOnCompletion {
            unregisterCompletedTask(id)
        }
    }

    fun unregisterCompletedTask(id: String) {
        val current = _activeTasksMap.value.toMutableMap()
        current.remove(id)
        _activeTasksMap.value = current
    }

    fun terminateTask(id: String) {
        val task = _activeTasksMap.value[id]
        task?.job?.cancel()
        task?.onTerminate?.invoke()
        unregisterCompletedTask(id)
    }

    fun terminateAll() {
        val tasks = _activeTasksMap.value.values.toList()
        tasks.forEach {
            it.job.cancel()
            it.onTerminate?.invoke()
        }
        _activeTasksMap.value = emptyMap()
    }

    fun purgeVram(repository: OllamaRepository = OllamaRepository(), hostUrl: String, modelName: String) {
        terminateAll()
        scope.launch(Dispatchers.IO) {
            repository.unloadModel(hostUrl, modelName)
        }
    }
}
