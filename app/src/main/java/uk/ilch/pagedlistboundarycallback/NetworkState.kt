package uk.ilch.pagedlistboundarycallback


/**
 * Created by Olexandr Ilchuk on 2/26/18.
 */
class NetworkState private constructor(val status: Status, var message: String?) {
    companion object {

        val LOADING = loading()
        val SUCCESS = success()
        val ERROR = this::error

        private fun loading(): NetworkState {
            return NetworkState(Status.LOADING, null)
        }

        private fun success(): NetworkState {
            return NetworkState(Status.SUCCESS, null)
        }

        private fun error(message: String): NetworkState {
            return NetworkState(Status.ERROR, message)
        }
    }
}
