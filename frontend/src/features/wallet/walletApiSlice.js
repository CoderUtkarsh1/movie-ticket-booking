import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const walletApiSlice = createApi({
  reducerPath: 'walletApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Wallet'],
  endpoints: (builder) => ({
    getWalletBalance: builder.query({
      query: () => '/payments/wallet/balance',
      transformResponse: (response) => response.data,
      providesTags: ['Wallet'],
    }),
    getWalletTransactions: builder.query({
      query: () => '/payments/wallet/transactions',
      transformResponse: (response) => response.data,
      providesTags: ['Wallet'],
    }),
    addMoney: builder.mutation({
      query: (amountData) => ({
        url: '/payments/wallet/add',
        method: 'POST',
        body: amountData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Wallet'],
    }),
  }),
});

export const {
  useGetWalletBalanceQuery,
  useGetWalletTransactionsQuery,
  useAddMoneyMutation,
} = walletApiSlice;
