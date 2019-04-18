package com.kaicomsol.kpos.dbhelper;

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */

class TransactionRepository {

    private TransactionDao mTransactionDao;
    private LiveData<List<Transaction>> mAllTransaction;
    private LiveData<Transaction> mTransaction;

    // Note that in order to unit test the TransactionRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    TransactionRepository(Application application) {
        TransactionRoomDatabase db = TransactionRoomDatabase.getDatabase(application);
        mTransactionDao = db.transactionDao();
        mAllTransaction = mTransactionDao.getAllTransaction();

    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Transaction>> getAllTransaction() {
        return mAllTransaction;
    }

    LiveData<Transaction> getTransactionByCardIdm(String cardIdm) {
        mTransaction = mTransactionDao.getTransactionByCardIdm(cardIdm);
        return mTransaction;
    }

    // You must call this on a non-UI thread or your app will crash.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    void insert(Transaction transaction) {
        new insertAsyncTask(mTransactionDao).execute(transaction);
    }

    void deleteByPaymentId(int paymentId){
        new deleteByPaymentIdAsyncTask(mTransactionDao).execute(paymentId);
    }

    void deleteAll() {
        new deleteAllAsyncTask(mTransactionDao).execute();
    }

    void deleteTransaction(Transaction transaction) {
        new deleteAsyncTask(mTransactionDao).execute(transaction);
    }

    private static class insertAsyncTask extends AsyncTask<Transaction, Void, Void> {

        private TransactionDao mAsyncTaskDao;

        insertAsyncTask(TransactionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Transaction... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Transaction, Void, Void> {

        private TransactionDao mAsyncTaskDao;

        deleteAsyncTask(TransactionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Transaction... params) {
            mAsyncTaskDao.deleteTransaction(params[0]);
            return null;
        }
    }

    private static class deleteByPaymentIdAsyncTask extends AsyncTask<Integer, Void, Void> {

        private TransactionDao mAsyncTaskDao;

        deleteByPaymentIdAsyncTask(TransactionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            mAsyncTaskDao.deleteByPaymentId(params[0]);
            return null;
        }
    }


    private static class deleteAllAsyncTask extends AsyncTask<Transaction, Void, Void> {

        private TransactionDao mAsyncTaskDao;

        deleteAllAsyncTask(TransactionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Transaction... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
}
