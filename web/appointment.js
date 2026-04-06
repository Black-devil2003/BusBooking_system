/* ====================================================
   APPOINTMENT JS — appointment.js
   Handles: validation, AJAX submit, preview, slot mgmt,
            bookings list, admin dashboard
   ==================================================== */

'use strict';

// ============================================================
// CONSTANTS
// ============================================================
const ALL_SLOTS = [
    { label: '9:00 AM',  sub: '9:00–9:30'  },
    { label: '9:30 AM',  sub: '9:30–10:00' },
    { label: '10:00 AM', sub: '10:00–10:30'},
    { label: '10:30 AM', sub: '10:30–11:00'},
    { label: '11:00 AM', sub: '11:00–11:30'},
    { label: '11:30 AM', sub: '11:30–12:00'},
    { label: '12:00 PM', sub: '12:00–12:30'},
    { label: '2:00 PM',  sub: '2:00–2:30'  },
    { label: '2:30 PM',  sub: '2:30–3:00'  },
    { label: '3:00 PM',  sub: '3:00–3:30'  },
    { label: '3:30 PM',  sub: '3:30–4:00'  },
    { label: '4:00 PM',  sub: '4:00–4:30'  },
    { label: '4:30 PM',  sub: '4:30–5:00'  },
    { label: '5:00 PM',  sub: '5:00–5:30'  },
];

// ============================================================
// DOM REFS
// ============================================================
const $form        = document.getElementById('appointmentForm');
const $name        = document.getElementById('appt-name');
const $email       = document.getElementById('appt-email');
const $phone       = document.getElementById('appt-phone');
const $date        = document.getElementById('appt-date');
const $slotHidden  = document.getElementById('appt-slot');
const $slotGrid    = document.getElementById('slot-grid');
const $submitBtn   = document.getElementById('appt-submit-btn');
const $btnText     = document.getElementById('appt-btn-text');
const $btnLoader   = document.getElementById('appt-btn-loader');
const $preview     = document.getElementById('appt-preview');
const $infoPanel   = document.getElementById('appt-info-panel');

// ============================================================
// UTILITY: toast
// ============================================================
function apptToast(msg, type = 'success') {
    const container = document.getElementById('appt-toast-container');
    const t = document.createElement('div');
    t.className = `appt-toast toast-${type}`;
    const icons = { success: 'fa-check-circle', error: 'fa-times-circle', info: 'fa-info-circle' };
    t.innerHTML = `<i class="fas ${icons[type] || icons.info}"></i><span>${msg}</span>`;
    container.appendChild(t);
    requestAnimationFrame(() => t.classList.add('toast-show'));
    setTimeout(() => {
        t.classList.remove('toast-show');
        setTimeout(() => t.remove(), 400);
    }, 4500);
}

// ============================================================
// TABS
// ============================================================
window.switchTab = function (tabName) {
    document.querySelectorAll('.appt-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.appt-tab-content').forEach(c => c.classList.remove('active'));
    document.getElementById(`tab-${tabName}`).classList.add('active');
    document.getElementById(`content-${tabName}`).classList.add('active');

    if (tabName === 'list')  loadBookingsList();
    if (tabName === 'admin') loadAdminDashboard();
};

// ============================================================
// SET MIN DATE (today)
// ============================================================
(function initDateMin() {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm   = String(today.getMonth() + 1).padStart(2, '0');
    const dd   = String(today.getDate()).padStart(2, '0');
    $date.min = `${yyyy}-${mm}-${dd}`;
})();

// ============================================================
// SLOT GRID — build & refresh taken slots
// ============================================================
let takenSlots = new Set();

function buildSlotGrid() {
    $slotGrid.innerHTML = '';
    ALL_SLOTS.forEach(slot => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'slot-btn' + (takenSlots.has(slot.label) ? ' slot-taken' : '');
        btn.dataset.slot = slot.label;
        btn.innerHTML = `${slot.label}<small>${slot.sub}</small>`;
        if (!takenSlots.has(slot.label)) {
            btn.addEventListener('click', () => selectSlot(slot.label, btn));
        }
        $slotGrid.appendChild(btn);
    });
}

function selectSlot(label, btn) {
    document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('slot-selected'));
    btn.classList.add('slot-selected');
    $slotHidden.value = label;
    clearError('grp-slot', 'err-slot');
}

async function refreshTakenSlots(dateVal) {
    if (!dateVal) return;
    try {
        const res  = await fetch(`/api/appointments/taken-slots?date=${encodeURIComponent(dateVal)}`);
        const data = await res.json();
        takenSlots = new Set(data.takenSlots || []);
    } catch (_) {
        takenSlots = new Set();
    }
    // Remember old selection
    const oldSel = $slotHidden.value;
    buildSlotGrid();
    // Re-apply selection if not taken
    if (oldSel && !takenSlots.has(oldSel)) {
        const btn = $slotGrid.querySelector(`[data-slot="${oldSel}"]`);
        if (btn) { btn.classList.add('slot-selected'); }
    } else {
        $slotHidden.value = '';
    }
}

$date.addEventListener('change', () => refreshTakenSlots($date.value));

// Initial build
buildSlotGrid();

// ============================================================
// VALIDATION HELPERS
// ============================================================
function setError(grpId, errId, msg) {
    document.getElementById(grpId).classList.add('has-error');
    document.getElementById(grpId).classList.remove('has-success');
    document.getElementById(errId).textContent = msg;
}

function setSuccess(grpId, errId) {
    document.getElementById(grpId).classList.remove('has-error');
    document.getElementById(grpId).classList.add('has-success');
    document.getElementById(errId).textContent = '';
}

function clearError(grpId, errId) {
    document.getElementById(grpId).classList.remove('has-error', 'has-success');
    document.getElementById(errId).textContent = '';
}

function validateAll() {
    let ok = true;

    // Name
    if (!$name.value.trim() || $name.value.trim().length < 2) {
        setError('grp-name', 'err-name', 'Please enter your full name (min 2 characters).');
        ok = false;
    } else { setSuccess('grp-name', 'err-name'); }

    // Email
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRe.test($email.value.trim())) {
        setError('grp-email', 'err-email', 'Please enter a valid email address.');
        ok = false;
    } else { setSuccess('grp-email', 'err-email'); }

    // Phone
    const phoneRe = /^[6-9]\d{9}$/;
    if (!phoneRe.test($phone.value.trim())) {
        setError('grp-phone', 'err-phone', 'Enter a valid 10-digit Indian mobile number.');
        ok = false;
    } else { setSuccess('grp-phone', 'err-phone'); }

    // Date
    if (!$date.value) {
        setError('grp-date', 'err-date', 'Please choose an appointment date.');
        ok = false;
    } else { setSuccess('grp-date', 'err-date'); }

    // Slot
    if (!$slotHidden.value) {
        setError('grp-slot', 'err-slot', 'Please select a time slot.');
        ok = false;
    } else { clearError('grp-slot', 'err-slot'); }

    return ok;
}

// Inline validation on blur
[$name, $email, $phone, $date].forEach(el => {
    el.addEventListener('blur', validateAll);
    el.addEventListener('input', () => {
        const id = el.id.replace('appt-', '');
        clearError(`grp-${id}`, `err-${id}`);
    });
});

// ============================================================
// FORM SUBMIT — AJAX
// ============================================================
$form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validateAll()) {
        apptToast('Please fix the highlighted fields.', 'error');
        return;
    }

    // Loading state
    $submitBtn.disabled = true;
    $btnText.style.display   = 'none';
    $btnLoader.style.display = 'flex';

    const payload = {
        name:     $name.value.trim(),
        email:    $email.value.trim(),
        phone:    $phone.value.trim(),
        date:     $date.value,
        timeSlot: $slotHidden.value
    };

    try {
        const res  = await fetch('/api/appointments/book', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        });
        const data = await res.json();

        if (!data.success) throw new Error(data.message || 'Booking failed.');

        // Show preview
        showPreview(data.booking);
        apptToast(`Booking confirmed! ID: ${data.booking.bookingId}`, 'success');

        // Also refresh taken slots for this date
        await refreshTakenSlots($date.value);

    } catch (err) {
        apptToast(err.message || 'Server error. Please try again.', 'error');
    } finally {
        $submitBtn.disabled = false;
        $btnText.style.display   = 'flex';
        $btnLoader.style.display = 'none';
    }
});

// ============================================================
// PREVIEW
// ============================================================
function showPreview(bk) {
    document.getElementById('preview-booking-id').textContent = bk.bookingId;
    document.getElementById('preview-name').textContent       = bk.name;
    document.getElementById('preview-email').textContent      = bk.email;
    document.getElementById('preview-phone').textContent      = bk.phone;
    document.getElementById('preview-date').textContent       = formatDate(bk.date || bk.apptDate);
    document.getElementById('preview-slot').textContent       = bk.timeSlot;

    $preview.classList.add('visible');
    $infoPanel.style.display = 'none';
    $preview.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function formatDate(raw) {
    if (!raw) return '--';
    const d = new Date(raw);
    return d.toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
}

window.resetForm = function () {
    $form.reset();
    $slotHidden.value = '';
    takenSlots = new Set();
    buildSlotGrid();
    $preview.classList.remove('visible');
    $infoPanel.style.display = 'block';
    document.querySelectorAll('.appt-form-group').forEach(g => {
        g.classList.remove('has-error', 'has-success');
    });
    document.querySelectorAll('.appt-error').forEach(e => e.textContent = '');
};

// ============================================================
// BOOKINGS LIST
// ============================================================
window.loadBookingsList = async function () {
    const tbody = document.getElementById('bookings-tbody');
    const countEl = document.getElementById('bookings-count');
    tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row"><i class="fas fa-spinner fa-spin"></i><p>Loading from database…</p></td></tr>`;

    try {
        const res  = await fetch('/api/appointments/all');
        const data = await res.json();
        const list = data.appointments || [];

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row"><i class="fas fa-inbox"></i><p>No bookings found in the database yet.</p></td></tr>`;
            countEl.textContent = '0 records';
            return;
        }

        tbody.innerHTML = list.map((b, i) => `
            <tr class="${i === 0 ? 'appt-newest' : ''}">
                <td>${i + 1}</td>
                <td><strong>${b.booking_id}</strong></td>
                <td>${escHtml(b.name)}</td>
                <td>${escHtml(b.email)}</td>
                <td>${escHtml(b.phone)}</td>
                <td>${formatDate(b.appt_date)}</td>
                <td>${escHtml(b.time_slot)}</td>
                <td><span class="badge-${(b.status || 'confirmed').toLowerCase()}">${b.status || 'Confirmed'}</span></td>
                <td>${formatTs(b.created_at)}</td>
            </tr>
        `).join('');

        countEl.textContent = `${list.length} record${list.length !== 1 ? 's' : ''} — fetched from MySQL`;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row"><i class="fas fa-exclamation-triangle"></i><p>Failed to fetch: ${escHtml(err.message)}</p></td></tr>`;
    }
};

// ============================================================
// ADMIN DASHBOARD
// ============================================================
window.loadAdminDashboard = async function () {
    // Reset stats
    ['stat-total','stat-confirmed','stat-today','stat-unique'].forEach(id => {
        document.getElementById(id).textContent = '…';
    });
    const tbody   = document.getElementById('admin-tbody');
    const countEl = document.getElementById('admin-count');
    tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row"><i class="fas fa-spinner fa-spin"></i><p>Loading from database…</p></td></tr>`;

    try {
        const [allRes, statsRes] = await Promise.all([
            fetch('/api/appointments/all'),
            fetch('/api/appointments/stats')
        ]);
        const allData   = await allRes.json();
        const statsData = await statsRes.json();
        const list = allData.appointments || [];
        const stats = statsData.stats || {};

        // Update stat cards
        document.getElementById('stat-total').textContent     = stats.total     ?? list.length;
        document.getElementById('stat-confirmed').textContent = stats.confirmed ?? '--';
        document.getElementById('stat-today').textContent     = stats.today     ?? '--';
        document.getElementById('stat-unique').textContent    = stats.unique    ?? '--';

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row"><i class="fas fa-inbox"></i><p>No records in the appointments table yet.</p></td></tr>`;
            countEl.textContent = '0 records';
            return;
        }

        tbody.innerHTML = list.map((b, i) => `
            <tr>
                <td>${b.id}</td>
                <td><strong>${b.booking_id}</strong></td>
                <td>${escHtml(b.name)}</td>
                <td>${escHtml(b.email)}</td>
                <td>${escHtml(b.phone)}</td>
                <td>${formatDate(b.appt_date)}</td>
                <td>${escHtml(b.time_slot)}</td>
                <td><span class="badge-${(b.status||'confirmed').toLowerCase()}">${b.status||'Confirmed'}</span></td>
                <td>${formatTs(b.created_at)}</td>
            </tr>
        `).join('');

        countEl.textContent = `${list.length} total record${list.length !== 1 ? 's' : ''} in appointments table`;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row"><i class="fas fa-exclamation-triangle"></i><p>Failed to load: ${escHtml(err.message)}</p></td></tr>`;
    }
};

// ============================================================
// HELPERS
// ============================================================
function escHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function formatTs(ts) {
    if (!ts) return '--';
    return new Date(ts).toLocaleString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}
