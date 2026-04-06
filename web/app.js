const routes = [];
let selectedRoute = JSON.parse(sessionStorage.getItem('booking_selected_route')) || null;
const appointmentSlots = [
    { label: '9:00 AM', sub: '9:00–9:30' },
    { label: '9:30 AM', sub: '9:30–10:00' },
    { label: '10:00 AM', sub: '10:00–10:30' },
    { label: '10:30 AM', sub: '10:30–11:00' },
    { label: '11:00 AM', sub: '11:00–11:30' },
    { label: '11:30 AM', sub: '11:30–12:00' },
    { label: '12:00 PM', sub: '12:00–12:30' },
    { label: '2:00 PM', sub: '2:00–2:30' },
    { label: '2:30 PM', sub: '2:30–3:00' },
    { label: '3:00 PM', sub: '3:00–3:30' },
    { label: '3:30 PM', sub: '3:30–4:00' },
    { label: '4:00 PM', sub: '4:00–4:30' },
    { label: '4:30 PM', sub: '4:30–5:00' },
    { label: '5:00 PM', sub: '5:00–5:30' },
];

// DOM Elements
const routeList = document.getElementById('route-list');
const searchBtn = document.getElementById('searchBtn');
const sourceInput = document.getElementById('source');
const destinationInput = document.getElementById('destination');
const modal = document.getElementById('bookingModal');
const closeModal = document.querySelector('.close-modal');
const bookingForm = document.getElementById('bookingForm');

// Initialize
async function init() {
    console.log('🚀 App initializing...');

    // Decoupled setup to prevent blocking
    try { checkAuth(); } catch (e) { console.error('Auth setup failed:', e); }
    try { setupEventListeners(); } catch (e) { console.error('Event listeners setup failed:', e); }
    try { setupAppointmentSPA(); } catch (e) { console.error('Appointment SPA setup failed:', e); }

    // INITIAL ROUTER CALL (Show UI based on URL immediately)
    router();

    // Background data fetching for routes
    try {
        console.log('Fetching all available routes...');
        const res = await fetch('/api/routes');
        if (!res.ok) throw new Error(`HTTP fetch failed: ${res.status}`);

        const data = await res.json();
        const routeData = data.routes ? data.routes : (Array.isArray(data) ? data : []);
        console.log(`Routes fetched: ${routeData.length}`);

        if (routeData && routeData.length > 0) {
            routes.length = 0;
            routeData.forEach(r => routes.push({
                id: r.routeId,
                source: r.source,
                destination: r.destination,
                fare: r.fare,
                duration: `${Math.floor(r.durationMinutes / 60)}h ${r.durationMinutes % 60}m`,
                image: r.source === 'Mumbai' ? 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?q=80&w=1374&auto=format&fit=crop' : 'https://images.unsplash.com/photo-1570125909232-eb263c188f7e?q=80&w=1471&auto=format&fit=crop'
            }));

            // Re-render views that depend on routes data
            renderRoutes(routes);

            // If we are on appointment page, re-trigger preview to find the route we just fetched
            if (window.location.pathname.startsWith('/appointments')) {
                renderSelectedRoutePreview();
            }
        }
    } catch (e) {
        console.error('Core app fetch error:', e);
        renderRoutes([]);
    }
}

// Check for existing session in localStorage
function checkAuth() {
    const sessionData = localStorage.getItem('authSession');
    if (sessionData) {
        try {
            const parsed = JSON.parse(sessionData);
            if (parsed && parsed.user) {
                setupUserSession(parsed.user);
            }
        } catch (e) {
            console.error('Failed to parse auth session:', e);
            localStorage.removeItem('authSession');
        }
    }
}

// Open Booking Flow
window.openBookingModal = async function (routeId) {
    selectedRoute = routes.find(r => r.id === routeId);
    if (!selectedRoute) return;

    // Fetch bus in background to show in preview
    try {
        const res = await fetch(`/api/buses?routeId=${routeId}`);
        const data = await res.json();
        const buses = data.buses || data;
        if (Array.isArray(buses) && buses.length > 0) {
            selectedRoute.busInfo = {
                name: `${buses[0].busType} (${buses[0].busNumber})`,
                time: buses[0].departureTime,
                busId: buses[0].busId
            };
        }
    } catch (e) {
        console.error('Bus info load error:', e);
    }

    // Save to session so it survives refresh
    sessionStorage.setItem('booking_selected_route', JSON.stringify(selectedRoute));

    // Navigate to appointment SPA section
    navigateTo('/appointments');

    // Render the summary at top
    renderSelectedRoutePreview();

    // Smooth scroll to form
    const container = document.getElementById('appointment-spa-container');
    if (container) container.scrollIntoView({ behavior: 'smooth' });
};

function renderSelectedRoutePreview() {
    const container = document.getElementById('selected-route-preview');
    if (!container) return;

    container.style.display = 'block';

    if (!selectedRoute) {
        container.innerHTML = `
            <div style="background: #fff1f2; border: 1px solid #fda4af; padding: 20px; border-radius: 12px; color: #991b1b; text-align: center; margin-bottom: 20px;">
                <i class="fas fa-exclamation-triangle" style="margin-right: 8px;"></i>
                No route selected. Please <a href="/routes" data-route style="color: #be123c; font-weight: 600;">choose a route first</a>.
            </div>
        `;
        document.getElementById('spaAppointmentForm').style.opacity = '0.5';
        document.getElementById('spaAppointmentForm').style.pointerEvents = 'none';
        return;
    }

    document.getElementById('spaAppointmentForm').style.opacity = '1';
    document.getElementById('spaAppointmentForm').style.pointerEvents = 'all';
    document.getElementById('spaAppointmentForm').style.display = 'block';
    document.getElementById('spa-appointment-success').style.display = 'none';

    container.innerHTML = `
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 15px; overflow: hidden; margin-bottom: 30px; display: flex; align-items: stretch; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
            <div style="width: 200px; position: relative;">
                <img src="${selectedRoute.image}" style="width: 100%; height: 100%; object-fit: cover;">
                <div style="position: absolute; top: 10px; right: 10px; background: #4f46e5; color: white; padding: 4px 12px; border-radius: 20px; font-weight: 600; font-size: 0.85rem;">
                    ₹${selectedRoute.fare}
                </div>
            </div>
            <div style="padding: 20px; flex: 1;">
                <div style="text-transform: uppercase; font-size: 0.75rem; font-weight: 700; color: #6366f1; margin-bottom: 5px;">Selected Route</div>
                <h3 style="font-size: 1.25rem; color: #1e1b4b; margin: 0 0 5px 0;">${selectedRoute.source} → ${selectedRoute.destination}</h3>
                
                ${selectedRoute.busInfo ? `
                    <div style="background: #eef2ff; padding: 10px 15px; border-radius: 10px; margin-bottom: 15px;">
                        <div style="font-weight: 600; color: #1e1b4b; font-size: 0.95rem;">${selectedRoute.busInfo.name}</div>
                        <div style="font-size: 0.85rem; color: #4f46e5;"><i class="fas fa-clock"></i> Departure: ${selectedRoute.busInfo.time}</div>
                    </div>
                ` : ''}

                <div style="display: flex; gap: 20px;">
                    <span style="font-size: 0.9rem; color: #64748b;"><i class="far fa-clock" style="margin-right: 5px;"></i> ${selectedRoute.duration}</span>
                    <span style="font-size: 0.9rem; color: #64748b;"><i class="fas fa-bus" style="margin-right: 5px;"></i> Premium Bus</span>
                </div>
            </div>
        </div>
    `;
}

// Render Routes
function renderRoutes(routesToRender) {
    const routeList = document.getElementById('route-list');
    const homePreview = document.getElementById('home-route-preview');

    if (routeList) routeList.innerHTML = '';
    if (homePreview) homePreview.innerHTML = '';

    if (routesToRender.length === 0) {
        if (routeList) routeList.innerHTML = '<div class="no-results">No routes found matching your search.</div>';
        if (homePreview) homePreview.innerHTML = '<div class="no-results">No popular routes found.</div>';
        return;
    }

    routesToRender.forEach((route, index) => {
        const cardHtml = `
            <div class="route-img">
                <img src="${route.image}" alt="${route.source} to ${route.destination}">
                <div class="route-price">Rs. ${route.fare}</div>
            </div>
            <div class="route-details">
                <div class="route-locations">
                    <span>${route.source}</span>
                    <i class="fas fa-arrow-right" style="font-size: 0.8rem; color: var(--gray);"></i>
                    <span>${route.destination}</span>
                </div>
                <div class="route-meta">
                    <span><i class="far fa-clock"></i> ${route.duration}</span>
                    <span><i class="fas fa-bus"></i> Daily</span>
                </div>
                <button class="btn-book" onclick="openBookingModal(${route.id})">Book Now</button>
            </div>
        `;

        if (routeList) {
            const card = document.createElement('div');
            card.className = 'route-card';
            card.innerHTML = cardHtml;
            routeList.appendChild(card);
        }

        if (homePreview && index < 6) {
            const card = document.createElement('div');
            card.className = 'route-card';
            card.innerHTML = cardHtml;
            homePreview.appendChild(card);
        }
    });
}

// Event Listeners
function setupEventListeners() {
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            const source = (sourceInput ? sourceInput.value : '').toLowerCase().trim();
            const dest = (destinationInput ? destinationInput.value : '').toLowerCase().trim();

            const filtered = routes.filter(route => {
                const matchSource = !source || route.source.toLowerCase().includes(source);
                const matchDest = !dest || route.destination.toLowerCase().includes(dest);
                return matchSource && matchDest;
            });

            renderRoutes(filtered);

            // Navigate to routes page if not already there
            if (window.location.pathname !== '/routes') {
                navigateTo('/routes');
            } else {
                // Smooth scroll to results
                const routesSection = document.querySelector('.routes-section');
                if (routesSection) routesSection.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }

    const routeSearchInput = document.getElementById('routeSearchInput');
    if (routeSearchInput) {
        routeSearchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase().trim();
            const filtered = routes.filter(route =>
                route.source.toLowerCase().includes(term) ||
                route.destination.toLowerCase().includes(term)
            );
            renderRoutes(filtered);
        });
    }

    closeModal.addEventListener('click', () => {
        modal.style.display = 'none';
        modal.classList.remove('show');
        document.querySelector('.modal-body').style.display = 'block';
        const successContent = document.getElementById('modal-success-content');
        if (successContent) successContent.style.display = 'none';
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
            modal.classList.remove('show');
            document.querySelector('.modal-body').style.display = 'block';
            const successContent = document.getElementById('modal-success-content');
            if (successContent) successContent.style.display = 'none';
        }
    });

    ;

    if (bookingForm) {
        bookingForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.querySelector('.btn-confirm');
            const originalText = btn.textContent;

            const busId = parseInt(document.getElementById('modal-bus-id')?.value || 1);
            const seatNumber = document.getElementById('selected-seat-display').textContent;
            const passengerName = document.getElementById('passenger-name').value.trim();
            const phone = document.getElementById('passenger-phone').value.trim();
            const email = document.getElementById('passenger-email').value.trim();

            if (seatNumber === '--') {
                showToast('Please select a seat first.', 'error');
                return;
            }

            btn.textContent = 'Processing...';
            btn.disabled = true;

            try {
                const res = await fetch('/api/book', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        busId,
                        seatNumber,
                        passengerName,
                        phone,
                        email
                    })
                });

                const data = await res.json();

                if (!data.success) {
                    throw new Error(data.message || 'Booking failed on server');
                }

                // Success UI
                document.querySelector('.modal-body').style.display = 'none';
                document.getElementById('modal-success-content').style.display = 'block';

                const routeInfo = document.getElementById('modal-route-info').textContent;
                const fareInfo = document.getElementById('modal-bus-fare').textContent;

                document.getElementById('success-route-info').textContent = routeInfo;
                document.getElementById('success-seat-info').textContent = seatNumber;
                document.getElementById('success-fare-info').textContent = fareInfo;

                bookingForm.reset();

                // Refresh bookings list if on bookings page
                // (Note: In a full app, we would fetch from backend here. For now, we'll still keep the local array for immediate display)
                const bookingObj = {
                    id: data.bookingId || ('BKG' + Math.floor(Math.random() * 1000000)),
                    route: routeInfo,
                    seat: seatNumber,
                    fare: fareInfo,
                    date: new Date().toLocaleDateString()
                };
                bookedTickets.push(bookingObj);
                renderBookings();

                showToast('Ticket successfully booked! It is now saved in the database.', 'success');
            } catch (err) {
                console.error('Booking error:', err);
                showToast(err.message || 'Connection error. Please try again.', 'error');
            } finally {
                btn.textContent = originalText;
                btn.disabled = false;
            }
        });
    }

    const closeSuccessBtn = document.getElementById('close-success-btn');
    if (closeSuccessBtn) {
        closeSuccessBtn.addEventListener('click', () => {
            modal.style.display = 'none';
            modal.classList.remove('show');
            document.querySelector('.modal-body').style.display = 'block';
            document.getElementById('modal-success-content').style.display = 'none';

            // Navigate to bookings page
            navigateTo('/bookings');
        });
    }
}



// Auth Logic
const signInBtn = document.getElementById('signInBtn');
const authModal = document.getElementById('authModal');
const closeAuth = document.querySelector('.close-auth');
const authTabs = document.querySelectorAll('.auth-tab');
const authForms = document.querySelectorAll('.auth-form');
const loginForm = document.getElementById('loginForm');
const signupForm = document.getElementById('signupForm');

signInBtn.addEventListener('click', () => {
    authModal.style.display = 'flex';
    setTimeout(() => authModal.classList.add('show'), 10);
});

closeAuth.addEventListener('click', () => {
    authModal.style.display = 'none';
    authModal.classList.remove('show');
});

window.addEventListener('click', (e) => {
    if (e.target === authModal) {
        authModal.style.display = 'none';
        authModal.classList.remove('show');
    }
});

authTabs.forEach(tab => {
    tab.addEventListener('click', () => {
        authTabs.forEach(t => t.classList.remove('active'));
        authForms.forEach(f => f.classList.remove('active'));

        tab.classList.add('active');
        const target = tab.dataset.tab;
        if (target === 'login') {
            loginForm.classList.add('active');
        } else {
            signupForm.classList.add('active');
        }
    });
});

// Helper: Extract and clean a display name from an email address
function cleanNameFromEmail(email) {
    const prefix = email.split('@')[0]; // e.g. "john123" or "jane.doe456"
    const cleaned = prefix.replace(/[^a-zA-Z]/g, ''); // Remove digits and special chars
    if (!cleaned) return 'User';
    // Capitalize first letter
    return cleaned.charAt(0).toUpperCase() + cleaned.slice(1).toLowerCase();
}

// Helper: Generate avatar initials from a display name
function getInitials(name) {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.charAt(0).toUpperCase();
}

// Helper: Title-case a name string
function toTitleCase(str) {
    return str.replace(/\w\S*/g, (word) =>
        word.charAt(0).toUpperCase() + word.substr(1).toLowerCase()
    );
}

async function handleAuth(e, type) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const originalText = btn.textContent;

    btn.textContent = 'Verifying...';
    btn.disabled = true;

    try {
        if (type === 'signup') {
            const name = signupForm.querySelector('input[type="text"]').value.trim();
            const email = signupForm.querySelector('input[type="email"]').value.trim();
            const password = signupForm.querySelectorAll('input[type="password"]')[0].value;

            const res = await fetch('/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password })
            });
            const data = await res.json();

            if (!data.success) throw new Error(data.message);

            showToast('Account created successfully. Please log in.', 'success');
            signupForm.reset();

            // Switch to login tab
            document.querySelector('.auth-tab[data-tab="login"]').click();

        } else {
            const email = loginForm.querySelector('input[type="email"]').value.trim();
            const password = loginForm.querySelector('input[type="password"]').value;

            const res = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            const data = await res.json();

            if (!data.success) throw new Error(data.message);

            // Save session to localStorage
            const sessionData = {
                token: data.token,
                user: {
                    name: data.user.name,
                    email: data.user.email,
                    initials: getInitials(data.user.name)
                }
            };
            localStorage.setItem('authSession', JSON.stringify(sessionData));

            // Set app state
            setupUserSession(sessionData.user);

            authModal.style.display = 'none';
            authModal.classList.remove('show');
            showToast('Logged In Successfully!', 'success');
        }
    } catch (error) {
        showToast(error.message || 'Authentication failed. Please try again.', 'error');
    } finally {
        btn.textContent = originalText;
        btn.disabled = false;
    }
}

function setupUserSession(user) {
    isLoggedIn = true;
    currentUser = user;

    // Use current name to generate initials
    const initials = getInitials(user.name);

    // Remove any "please sign in" prompt
    const authPrompt = document.getElementById('auth-booking-prompt');
    if (authPrompt) authPrompt.remove();

    // Update UI
    const nav = document.querySelector('nav');
    signInBtn.style.display = 'none';

    // Remove any existing profile element
    const existingProfile = document.querySelector('.user-profile');
    if (existingProfile) existingProfile.remove();

    const userProfile = document.createElement('div');
    userProfile.className = 'user-profile';
    userProfile.id = 'userProfileBtn';
    userProfile.innerHTML = `
        <div class="user-avatar">${initials}</div>
        <span style="font-weight: 500;">${user.name}</span>
    `;
    userProfile.addEventListener('click', openProfilePage);
    nav.appendChild(userProfile);
}

loginForm.addEventListener('submit', (e) => handleAuth(e, 'login'));
signupForm.addEventListener('submit', (e) => handleAuth(e, 'signup'));

// The old renderSeatMap and openBookingModal functions are no longer used.
// Bus and seat selection is now integrated into the SPA appointment flow.


// Toast Notification Logic
window.showToast = function (message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';

    toast.innerHTML = `
        <i class="fas ${icon}"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    // Trigger animation
    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    // Remove after 4 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 400); // Wait for transition
    }, 4000);
};

// State
let bookedTickets = [];
let isLoggedIn = false;
let currentUser = { name: '', email: '', initials: '' };

async function renderBookings() {
    const list = document.getElementById('bookings-list');
    if (!list) return;

    // Allow viewing if we have a phone number (even as a guest)
    if (!isLoggedIn && !currentUser.phone) {
        list.innerHTML = '<div class="no-results" style="text-align: center; padding: 40px;"><p>Please log in or book an appointment to view your bookings.</p><button onclick="document.getElementById(\'signInBtn\').click()" class="btn-primary" style="margin-top: 15px;">Sign In</button></div>';
        return;
    }

    try {
        const phone = currentUser.phone || '';
        const email = currentUser.email || '';
        console.log(`Fetching bookings for phone: "${phone}", email: "${email}"`);

        // Fetch bookings matching either phone or email
        const res = await fetch(`/api/my-bookings?phone=${phone}&email=${email}`);
        const data = await res.json();
        console.log('Bookings API response:', data);

        const bookings = Array.isArray(data.bookings) ? data.bookings : (Array.isArray(data) ? data : []);
        console.log(`Found ${bookings.length} bookings in response.`);

        if (bookings.length === 0 && bookedTickets.length === 0) {
            list.innerHTML = `
                <div class="no-results" style="text-align: center; color: var(--gray); padding: 40px;">
                    <i class="fas fa-ticket-alt" style="font-size: 3rem; margin-bottom: 20px; opacity: 0.5;"></i>
                    <p>You haven't booked any tickets yet.</p>
                </div>
            `;
            return;
        }

        // Combine for best UX
        const allBookings = [...bookings, ...bookedTickets];

        list.innerHTML = '';
        allBookings.forEach(bkg => {
            const card = document.createElement('div');
            card.className = 'booking-card';

            // Normalize fields between legacy seat-booking and current appointment-booking
            const source = bkg.source || 'Unknown';
            const dest = bkg.destination || 'Unknown';
            let defaultRoute = (bkg.source || bkg.destination) ? `${source} -> ${dest}` : 'N/A';
            if (source === 'Unknown' && dest === 'Unknown') defaultRoute = 'Unknown Route';

            const route = bkg.route || defaultRoute;
            const bkgId = bkg.bookingId || bkg.booking_id || bkg.id || 'N/A';
            const date = bkg.apptDate || bkg.appt_date || bkg.date || bkg.travelDate || 'N/A';
            const timeInfo = bkg.timeSlot || bkg.time_slot || `Seat ${bkg.seatNumber || bkg.seat || '--'}`;
            const fare = bkg.fare ? (typeof bkg.fare === 'number' ? `Rs. ${bkg.fare}` : bkg.fare) : 'Rs. 0';

            card.innerHTML = `
                <div class="booking-info">
                    <h4>${route}</h4>
                    <p><strong>Booking ID:</strong> ${bkgId}</p>
                    <p><strong>Date:</strong> ${date} &nbsp;|&nbsp; <strong>Time/Seat:</strong> ${timeInfo}</p>
                    <p style="color: var(--primary); font-weight: 600;">Paid: ${fare}</p>
                </div>
                <div>
                    <span class="booking-status status-${(bkg.status || 'Confirmed').toLowerCase()}">${bkg.status || 'Confirmed'}</span>
                </div>
            `;
            list.appendChild(card);
        });
    } catch (e) {
        console.error('Failed to render real bookings:', e);
    }
}

// ========================
// Profile Page & Logout
// ========================
const profilePage = document.getElementById('profilePage');
const profileBackBtn = document.getElementById('profileBackBtn');
const logoutBtn = document.getElementById('logoutBtn');

async function openProfilePage() {
    // Populate profile page with current user data
    document.getElementById('profileAvatarLg').textContent = getInitials(currentUser.name);
    document.getElementById('profileFullName').textContent = currentUser.name;
    document.getElementById('profileEmail').innerHTML = `<i class="fas fa-envelope"></i> ${currentUser.email || 'Not provided'}`;
    
    // Fetch total bookings from database to ensure accuracy
    try {
        const phone = currentUser.phone || '';
        const email = currentUser.email || '';
        if (phone || email) {
            const res = await fetch(`/api/my-bookings?phone=${phone}&email=${email}`);
            const data = await res.json();
            const dbBookings = Array.isArray(data.bookings) ? data.bookings : [];
            // Merge with local bookedTickets to show immediate bookings too
            const totalCount = dbBookings.length;
            document.getElementById('profileTotalBookings').textContent = totalCount;
        } else {
            document.getElementById('profileTotalBookings').textContent = bookedTickets.length;
        }
    } catch (e) {
        console.error('Failed to fetch profile booking count:', e);
        document.getElementById('profileTotalBookings').textContent = bookedTickets.length;
    }

    // Set member since to current month/year
    const now = new Date();
    const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    document.getElementById('profileMemberSince').textContent = `${months[now.getMonth()]} ${now.getFullYear()}`;

    // Show profile page
    profilePage.style.display = 'flex';
    // Trigger animation after display is set
    requestAnimationFrame(() => {
        profilePage.classList.add('show');
    });
}

function closeProfilePage() {
    profilePage.classList.remove('show');
    setTimeout(() => {
        profilePage.style.display = 'none';
    }, 400);
}

function handleLogout() {
    // Close profile page
    closeProfilePage();

    setTimeout(() => {
        // Clear local storage session
        localStorage.removeItem('authSession');

        // Reset state
        isLoggedIn = false;
        currentUser = { name: '', email: '', initials: '' };
        bookedTickets = [];
        renderBookings();

        // Remove user profile from nav
        const userProfile = document.querySelector('.user-profile');
        if (userProfile) userProfile.remove();

        // Show sign in button again
        signInBtn.style.display = '';

        // Reset forms
        loginForm.reset();
        signupForm.reset();

        // Show auth modal (login page)
        authModal.style.display = 'flex';
        setTimeout(() => authModal.classList.add('show'), 10);

        // If currently on a protected route, go home
        if (window.location.pathname.startsWith('/bookings')) {
            navigateTo('/');
        }

        showToast('You have been logged out successfully.', 'success');
    }, 500);
}

// Profile page event listeners
profileBackBtn.addEventListener('click', closeProfilePage);
logoutBtn.addEventListener('click', handleLogout);

// ========================
// SPA Router
// ========================
const pages = document.querySelectorAll('.page-section');
const navLinks = document.querySelectorAll('a[data-route]');

window.navigateTo = function (url) {
    history.pushState(null, null, url);
    router();
};

function router() {
    const url = new URL(window.location.href);
    let path = url.pathname;
    let pageId = 'home'; // default

    if (path.startsWith('/routes')) pageId = 'routes';
    else if (path.startsWith('/appointments')) {
        pageId = 'appointments';

        // Handle route_id in query params if selectedRoute not yet set
        const routeIdParam = url.searchParams.get('route_id');
        if (routeIdParam && !selectedRoute) {
            const targetId = parseInt(routeIdParam);
            selectedRoute = routes.find(r => r.id === targetId);
            console.log('Route from URL param:', selectedRoute);
        }

        renderSelectedRoutePreview();
    }
    else if (path.startsWith('/bookings')) {
        pageId = 'bookings';
        renderBookings();
    }
    else if (path.startsWith('/contact')) pageId = 'contact';

    // Route Protection (Only strict for private features, keep simple for demo)
    if (pageId === 'bookings' && !isLoggedIn && !currentUser.phone) {
        showToast('Please log in to view your bookings.', 'error');
        authModal.style.display = 'flex';
        setTimeout(() => authModal.classList.add('show'), 10);

        // Ensure Login tab is active
        document.querySelector('.auth-tab[data-tab="login"]').click();

        // Redirect back home immediately
        history.replaceState(null, null, '/');
        pageId = 'home';
        path = '/';
    }

    // Update active nav links
    navLinks.forEach(link => {
        const url = link.getAttribute('href');
        if (url === path || (path === '/' && url === '/')) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Show active section template
    pages.forEach(page => {
        if (page.dataset.page === pageId) {
            page.classList.add('active');
        } else {
            page.classList.remove('active');
        }
    });

    // Scroll to top when page changes
    window.scrollTo(0, 0);
}

// SPA Appointment System
function setupAppointmentSPA() {
    const $spaForm = document.getElementById('spaAppointmentForm');
    const $spaDate = document.getElementById('spa-appt-date');
    const $spaSlotGrid = document.getElementById('spa-slot-grid');
    const $spaSlotHidden = document.getElementById('spa-selected-slot');

    if (!$spaForm) return;

    // Set min date to today
    const now = new Date();
    $spaDate.min = now.toISOString().split('T')[0];

    const renderSpaSlots = async () => {
        const date = $spaDate.value;
        console.log('Rendering slots for date:', date || 'None selected');
        $spaSlotGrid.innerHTML = '<p style="grid-column: 1/-1; text-align:center; color:#64748b;">Loading slots...</p>';

        let takenSlots = [];
        if (date) {
            try {
                const res = await fetch(`/api/appointments/taken-slots?date=${date}`);
                const data = await res.json();
                takenSlots = data.takenSlots || [];
                console.log('Taken slots from server:', takenSlots);
            } catch (e) { console.error('Error fetching slots:', e); }
        }

        $spaSlotGrid.innerHTML = '';
        appointmentSlots.forEach(slot => {
            const isTaken = takenSlots.includes(slot.label);
            const div = document.createElement('div');
            div.className = `slot-item ${isTaken ? 'taken' : ''}`;
            div.style.padding = '12px';
            div.style.border = '1px solid #e2e8f0';
            div.style.borderRadius = '10px';
            div.style.textAlign = 'center';
            div.style.cursor = isTaken ? 'not-allowed' : 'pointer';
            div.style.opacity = isTaken ? '0.5' : '1';
            div.style.background = isTaken ? '#f1f5f9' : 'white';

            div.innerHTML = `
                <div style="font-weight:600; font-size:0.9rem;">${slot.label}</div>
                <div style="font-size:0.7rem; color:#64748b;">${slot.sub}</div>
            `;

            if (!isTaken) {
                div.addEventListener('click', () => {
                    document.querySelectorAll('.slot-item').forEach(s => s.classList.remove('selected'));
                    div.classList.add('selected');
                    $spaSlotHidden.value = slot.label;
                    console.log('Selected slot:', slot.label);
                });
            }
            $spaSlotGrid.appendChild(div);
        });
    };

    ['change', 'input'].forEach(evt => {
        $spaDate.addEventListener(evt, () => {
            $spaSlotHidden.value = ''; // Reset selection
            renderSpaSlots();
        });
    });
    renderSpaSlots();

    $spaForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        console.log('Appointment form submitted');

        const name = document.getElementById('spa-appt-name').value.trim();
        const email = document.getElementById('spa-appt-email').value.trim();
        const phone = document.getElementById('spa-appt-phone').value.trim();
        const date = $spaDate.value;
        const timeSlot = $spaSlotHidden.value;

        console.log('Form data:', { name, email, phone, date, timeSlot });

        if (!date) {
            showToast('Please select a booking date.', 'error');
            $spaDate.focus();
            return;
        }

        if (!timeSlot) {
            showToast('Please click on a time slot below.', 'error');
            const grid = document.getElementById('spa-slot-grid');
            if (grid) grid.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return;
        }

        const btn = $spaForm.querySelector('button');
        const originalBtnText = btn.textContent;
        btn.disabled = true;
        btn.textContent = '⏳ Processing...';

        try {
            console.log('Sending request to /api/appointments/book...');
            const res = await fetch('/api/appointments/book', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name,
                    email,
                    phone,
                    date,
                    timeSlot,
                    routeId: selectedRoute?.id,
                    source: selectedRoute?.source,
                    destination: selectedRoute?.destination,
                    fare: selectedRoute?.fare,
                    busInfo: selectedRoute?.busInfo ? selectedRoute.busInfo.name : 'Unknown'
                })
            });

            const data = await res.json();
            console.log('Server response:', data);

            if (data.success) {
                console.log('Booking successful! Showing success screen.');
                showToast(`Booking Successful!`, 'success');

                // Show success UI
                $spaForm.style.display = 'none';
                const preview = document.getElementById('selected-route-preview');
                if (preview) preview.style.display = 'none';

                const successDiv = document.getElementById('spa-appointment-success');
                if (successDiv) {
                    successDiv.style.display = 'block';
                    document.getElementById('spa-success-msg').textContent = `Your booking ID is ${data.booking.bookingId}. We have reserved your seat and sent details to your email.`;
                }

                // Ensure session has the phone number for viewing bookings
                isLoggedIn = true;
                currentUser = {
                    ...currentUser,
                    name: data.booking.name,
                    email: data.booking.email,
                    phone: data.booking.phone,
                    initials: currentUser.initials || name.split(' ').map(n => n[0]).join('').toUpperCase()
                };

                // Update header if exists
                setupUserSession(currentUser);

                // Save to localStorage so it persists refresh
                localStorage.setItem('authSession', JSON.stringify({ user: currentUser }));

                $spaForm.reset();
                $spaSlotHidden.value = '';
                selectedRoute = null;
                sessionStorage.removeItem('booking_selected_route');
                renderSpaSlots();
            } else {
                console.warn('Booking failed:', data.message);
                showToast(data.message || 'Booking failed. Please try again.', 'error');
            }
        } catch (e) {
            console.error('Fetch error:', e);
            showToast('Connection error. Is the server running?', 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = originalBtnText;
        }
    });
}


// Handle browser back/forward buttons
window.addEventListener('popstate', router);

// Intercept routing link clicks
document.body.addEventListener('click', e => {
    // Check if clicked element or its parent has data-route
    const link = e.target.closest('[data-route]');
    if (link) {
        e.preventDefault();
        navigateTo(link.getAttribute('href'));
    }
});

// Run
init();
