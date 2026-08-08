import { useState } from 'react';
import { useGetWalletBalanceQuery, useGetWalletTransactionsQuery, useAddMoneyMutation } from '../../features/wallet/walletApiSlice';
import './WalletPage.css';

const WalletPage = () => {
  const [amount, setAmount] = useState('');
  
  const { data: balanceData, isLoading: balanceLoading, refetch: refetchBalance } = useGetWalletBalanceQuery();
  const { data: transactionsData, isLoading: transactionsLoading, refetch: refetchTxns } = useGetWalletTransactionsQuery();
  const [addMoney, { isLoading: addingMoney }] = useAddMoneyMutation();

  const balance = balanceData?.balance || 0;
  const transactions = transactionsData || [];

  const handleAddMoney = async (e) => {
    e.preventDefault();
    if (!amount || isNaN(amount) || amount <= 0) {
      alert('Please enter a valid amount greater than 0');
      return;
    }

    try {
      await addMoney({ amount: parseFloat(amount) }).unwrap();
      setAmount('');
      alert(`₹${amount} added successfully!`);
      // Re-fetch explicitly to ensure UI is in sync (though RTK tags should handle it)
      refetchBalance();
      refetchTxns();
    } catch (err) {
      alert(err.data?.message || 'Failed to add money');
    }
  };

  return (
    <div className="wallet-page">
      <div className="dashboard-page-header">
        <h2>My Wallet</h2>
      </div>

      <div className="wallet-grid">
        {/* Balance Card */}
        <div className="wallet-balance-card glass-card">
          <div className="balance-info">
            <span className="balance-label">Available Balance</span>
            {balanceLoading ? (
              <div className="spinner spinner-sm mt-2"></div>
            ) : (
              <h1 className="balance-amount">₹{parseFloat(balance).toFixed(2)}</h1>
            )}
          </div>

          <form className="add-money-form" onSubmit={handleAddMoney}>
            <div className="input-group">
              <span className="currency-symbol">₹</span>
              <input
                type="number"
                className="form-control"
                placeholder="Enter Amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                min="1"
                step="1"
              />
            </div>
            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={addingMoney || !amount}
            >
              {addingMoney ? 'Adding...' : 'Add Money'}
            </button>
          </form>
        </div>

        {/* Transactions List */}
        <div className="wallet-transactions glass-card">
          <div className="transactions-header">
            <h3>Recent Transactions</h3>
          </div>
          
          <div className="transactions-list">
            {transactionsLoading ? (
              <div className="loader-container"><div className="spinner"></div></div>
            ) : transactions.length > 0 ? (
              transactions.map(txn => (
                <div key={txn.id} className="transaction-item">
                  <div className="txn-icon">
                    {txn.type === 'CREDIT' ? (
                      <div className="icon-credit">↓</div>
                    ) : (
                      <div className="icon-debit">↑</div>
                    )}
                  </div>
                  <div className="txn-details">
                    <p className="txn-desc">{txn.description}</p>
                    <span className="txn-date">{new Date(txn.createdAt).toLocaleString()}</span>
                    {txn.referenceId && <span className="txn-ref">Ref: {txn.referenceId}</span>}
                  </div>
                  <div className="txn-amount-col">
                    <span className={`txn-amount ${txn.type.toLowerCase()}`}>
                      {txn.type === 'CREDIT' ? '+' : '-'}₹{txn.amount}
                    </span>
                    <span className="txn-balance">Balance: ₹{txn.balanceAfter}</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <p>No transactions yet.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default WalletPage;
