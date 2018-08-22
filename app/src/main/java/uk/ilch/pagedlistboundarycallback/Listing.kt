package uk.ilch.pagedlistboundarycallback

import androidx.lifecycle.LiveData
import androidx.paging.PagedList


/**
 * Created by Olexandr Ilchuk on 2/26/18.
 */
class Listing<T> constructor(
        val pagedList: LiveData<PagedList<T>>,
        val networkState: LiveData<NetworkState>,
        val retry: Runnable)