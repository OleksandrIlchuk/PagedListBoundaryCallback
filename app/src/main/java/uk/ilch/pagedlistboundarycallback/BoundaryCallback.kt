package uk.ilch.pagedlistboundarycallback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import timber.log.Timber
import java.util.concurrent.Executor

/**
 * Created by Olexandr Ilchuk on 7/3/18.
 */
abstract class BoundaryCallback<RequestType, ResultType>(private val executor: Executor) : PagedList.BoundaryCallback<RequestType>() {

    var retry: Runnable? = null
    val networkState = MutableLiveData<NetworkState>()

    @MainThread
    override fun onZeroItemsLoaded() {
        Timber.d("onZeroItemsLoaded")
        fetchFromNetwork(true, null)
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: RequestType) {
        Timber.d("onItemAtEndLoaded")
        fetchFromNetwork(false, itemAtEnd)
    }

    @MainThread
    private fun fetchFromNetwork(isInitial: Boolean, itemAtEnd: RequestType?) {
        networkState.postValue(NetworkState.LOADING)
        val apiResponse = createCall(itemAtEnd)

        val observer = object : Observer<Response<ResultType>> {
            override fun onChanged(response: Response<ResultType>?) {
                apiResponse.removeObserver(this)

                if (response!!.isSuccessful()) {
                    retry = null
                    networkState.postValue(NetworkState.SUCCESS)
                    saveCallResult(processResponse(response))
                } else {
                    retry = Runnable { fetchFromNetwork(isInitial, itemAtEnd) }
                    networkState.postValue(NetworkState.ERROR(response.errorMessage!!))
                }
            }
        }

        apiResponse.observeForever(observer)
    }

    protected open fun processResponse(response: Response<ResultType>): ResultType {
        return response.body!!
    }

    @WorkerThread
    protected abstract fun saveCallResult(result: ResultType)

    @MainThread
    protected abstract fun createCall(itemAtEnd: RequestType?): LiveData<Response<ResultType>>

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        if (prevRetry != null) {
            executor.execute(prevRetry)
        }
    }
}