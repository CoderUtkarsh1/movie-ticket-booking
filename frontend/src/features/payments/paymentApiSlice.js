import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const paymentApiSlice = createApi({
  reducerPath: 'paymentApi',
  baseQuery: baseQueryWithAuth,
  endpoints: (builder) => ({
    processPayment: builder.mutation({
      query: (paymentData) => ({
        url: '/payments/process',
        method: 'POST',
        body: paymentData,
      }),
      transformResponse: (response) => response.data,
    }),
  }),
});

export const {
  useProcessPaymentMutation,
} = paymentApiSlice;
