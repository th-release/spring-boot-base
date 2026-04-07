document.addEventListener('DOMContentLoaded', () => {
    const statusEl = document.getElementById('status');
    const btn = document.getElementById('test-btn');

    statusEl.textContent = '✅ JavaScript is successfully loaded!';
    statusEl.style.color = '#28a745';
    statusEl.style.fontWeight = 'bold';

    btn.addEventListener('click', async () => {
        try {
            // Swagger나 Actuator 등 공개된 API 중 하나를 호출하여 확인
            const response = await fetch('/actuator/health');
            const data = await response.json();
            
            alert(`API Status: ${data.status}\nURL: ${window.location.origin}/actuator/health`);
        } catch (error) {
            alert('API call failed. Make sure the server is running!');
            console.error('Error fetching API:', error);
        }
    });

    console.log('SPA Routing Test: Try navigating to /test-path to see if it redirects to index.html');
});
