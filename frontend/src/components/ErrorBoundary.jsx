import { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '60vh',
          padding: '2rem',
          textAlign: 'center',
          color: 'var(--text-primary, #fff)'
        }}>
          <div style={{
            fontSize: '4rem',
            marginBottom: '1rem'
          }}>🎬</div>
          <h2 style={{
            fontSize: '1.5rem',
            marginBottom: '0.5rem',
            background: 'linear-gradient(135deg, #e74c3c, #c0392b)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>
            Oops! Something went wrong
          </h2>
          <p style={{
            color: 'var(--text-secondary, #aaa)',
            marginBottom: '1.5rem',
            maxWidth: '400px'
          }}>
            An unexpected error occurred. Please try refreshing the page.
          </p>
          <button
            onClick={() => window.location.reload()}
            style={{
              padding: '0.75rem 2rem',
              borderRadius: '8px',
              border: 'none',
              background: 'linear-gradient(135deg, #e74c3c, #c0392b)',
              color: '#fff',
              fontSize: '1rem',
              cursor: 'pointer',
              transition: 'transform 0.2s'
            }}
            onMouseOver={(e) => e.target.style.transform = 'scale(1.05)'}
            onMouseOut={(e) => e.target.style.transform = 'scale(1)'}
          >
            Refresh Page
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
