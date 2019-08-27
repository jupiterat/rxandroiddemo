/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.cheesefinder

import android.text.Editable
import android.text.TextWatcher
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cheeses.*
import java.util.concurrent.TimeUnit

//import kotlinx.android.synthetic.main.activity_cheeses.*


class CheeseActivity : BaseSearchActivity() {

    override fun onStart() {
        super.onStart()
//        demo1()
//        demo2()
//        demo3()
        demo4()
    }

    /**
     * UI is freezes because search is executed on main thread
     */
    private fun demo1() {
        val searchTextObservable = createButtonClickObservable()

        searchTextObservable
                // 2
                .subscribe { query ->
                    // 3
                    showResult(cheeseSearchEngine.search(query))
                }
    }

    /**
     * add
     * @subscribeOn: specifies the thread on which the observable will be subscribed (i.e. created).
     * @observeOn: specifies the thread on which the next operators in the chain will be executed
     */
    private fun demo2() {
        val searchTextObservable = createButtonClickObservable()


        searchTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{
                    showProgress()
                }
                .observeOn(Schedulers.io())
                .map {
                    cheeseSearchEngine.search(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hideProgress()
                    showResult(it)
                }

    }


    private fun demo3() {
        createTextChangeObservable()

        val searchTextObservable = createTextChangeObservable()


        searchTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{
                    showProgress()
                }
                .observeOn(Schedulers.io())
                .map {
                    cheeseSearchEngine.search(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hideProgress()
                    showResult(it)
                }
    }

    private fun demo4() {
        val buttonClickStream = createButtonClickObservable()
        val textChangeStream = createTextChangeObservable()
        val searchTextObservable = Observable.merge<String>(buttonClickStream, textChangeStream)
        searchTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{
                    showProgress()
                }
                .observeOn(Schedulers.io())
                .map {
                    cheeseSearchEngine.search(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hideProgress()
                    showResult(it)
                }
    }

    // 1
    private fun createButtonClickObservable(): Observable<String> {
        // 2
        return Observable.create { emitter ->
            // 3
            searchButton.setOnClickListener {
                // 4
                emitter.onNext(queryEditText.text.toString())
            }

            // 5
            emitter.setCancellable {
                // 6
                searchButton.setOnClickListener(null)
            }
        }
    }

    private fun createTextChangeObservable() : Observable<String> {
        var observable =  Observable.create<String> { emitter ->
            var textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun afterTextChanged(s: Editable?) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.toString()?.let { emitter.onNext(it) }
                }

            }

            queryEditText.addTextChangedListener(textWatcher)
            emitter.setCancellable {
                queryEditText.removeTextChangedListener(textWatcher)
            }
        }
        return observable.filter { it.length >= 2 }.debounce(1000, TimeUnit.MILLISECONDS)
    }

}