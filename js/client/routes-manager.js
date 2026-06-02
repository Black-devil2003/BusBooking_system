'use strict';
/* ============================================================
   ROUTE MANAGER JS — routes-manager.js
   Handles: add, edit, delete routes + book via route
   ============================================================ */

// ---- Time slots for booking ----
const SLOTS = [
    { label: '6:00 AM',  sub: '6:00–6:30'  },
    { label: '9:00 AM',  sub: '9:00–9:30'  },
    { label: '10:00 AM', sub: '10:00–10:30'},
    { label: '11:00 AM', sub: '11:00–11:30'},
    { label: '12:00 PM', sub: '12:00–12:30'},
    { label: '2:00 PM',  sub: '2:00–2:30'  },
    { label: '3:00 PM',  sub: '3:00–3:30'  },
    { label: '4:30 PM',  sub: '4:30–5:00'  },
    { label: '6:00 PM',  sub: '6:00–6:30'  },
    { label: '8:00 PM',  sub: '8:00–8:30'  },
    { label: '9:00 PM',  sub: '9:00–9:30'  },
    { label: '10:00 PM', sub: '10:00–10:30'},
];

// ---- State ----
let allRoutes = [];
let editingId = null;
let deleteTargetId = null;

// ---- Tab switching ----
window.rmTab = function (name) {
    document.querySelectorAll('.appt-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.rm-tab-content').forEach(c => c.classList.remove('active'));
    document.getElementById(`tab-${name}`).classList.add('active');
    document.getElementById(`content-${name}`).classList.add('active');
    if (name === 'routes') loadRoutesList();
    if (name === 'book')   loadBookRouteCards();
};

// ============================================================
// TOAST
// ============================================================
function toast(msg, type = 'success') {
    const c = document.getElementById('appt-toast-container');
    const t = document.createElement('div');
    t.className = `appt-toast toast-${type}`;
    const icons = { success: 'fa-check-circle', error: 'fa-times-circle', info: 'fa-info-circle' };
    t.innerHTML = `<i class="fas ${icons[type]||'fa-info-circle'}"></i><span>${msg}</span>`;
    c.appendChild(t);
    requestAnimationFrame(() => t.classList.add('toast-show'));
    setTimeout(() => { t.classList.remove('toast-show'); setTimeout(() => t.remove(), 400); }, 4500);
}

// ============================================================
// HERO STATS
// ============================================================
async function loadHeroStats() {
    try {
        const res  = await fetch('/api/manage/routes/stats');
        const data = await res.json();
        const s    = data.stats || {};
        document.getElementById('hs-total').textContent  = s.total  ?? '--';
        document.getElementById('hs-cities').textContent = s.cities ?? '--';
        document.getElementById('hs-today').textContent  = s.today  ?? '--';
    } catch (_) {}
}

// ============================================================
// LIVE PREVIEW
// ============================================================
const $rFrom     = document.getElementById('r-from');
const $rTo       = document.getElementById('r-to');
const $rName     = document.getElementById('r-name');
const $rFare     = document.getElementById('r-fare');
const $rDuration = document.getElementById('r-duration');
const $rDistance = document.getElementById('r-distance');

function updatePreview() {
    const from = $rFrom.value.trim()    || 'Origin';
    const to   = $rTo.value.trim()      || 'Destination';
    const dur  = $rDuration.value.trim();
    const dist = $rDistance.value.trim();

    document.getElementById('prev-from').textContent = from;
    document.getElementById('prev-to').textContent   = to;
    document.getElementById('prev-name').textContent = $rName.value.trim() || `${from} – ${to} Express Route`;
    document.getElementById('prev-fare').textContent = $rFare.value ? `₹ ${parseFloat($rFare.value).toLocaleString('en-IN')}` : '--';
    document.getElementById('prev-dur').textContent  = dur  ? `${dur} mins (${minsToHrs(parseInt(dur))})` : '-- mins';
    document.getElementById('prev-dist').textContent = dist ? `${dist} km` : '--';
}

function autoFillName() {
    const from = $rFrom.value.trim();
    const to   = $rTo.value.trim();
    if (from && to && !$rName.value.trim()) {
        $rName.value = `${from} – ${to} Express Route`;
    }
    updatePreview();
}

[$rFrom, $rTo, $rName, $rFare, $rDuration, $rDistance].forEach(el => el.addEventListener('input', updatePreview));
$rFrom.addEventListener('blur', autoFillName);
$rTo.addEventListener('blur', autoFillName);

function minsToHrs(m) {
    if (!m || isNaN(m)) return '--';
    return `${Math.floor(m/60)}h ${m%60}m`;
}

// ============================================================
// VALIDATION
// ============================================================
function setErr(grp, errId, msg) {
    document.getElementById(grp).classList.add('has-error');
    document.getElementById(grp).classList.remove('has-success');
    document.getElementById(errId).textContent = msg;
}
function setOk(grp, errId) {
    document.getElementById(grp).classList.remove('has-error');
    document.getElementById(grp).classList.add('has-success');
    document.getElementById(errId).textContent = '';
}
function clrField(grp, errId) {
    document.getElementById(grp).classList.remove('has-error','has-success');
    document.getElementById(errId).textContent = '';
}

function validateRouteForm() {
    let ok = true;
    if ($rFrom.value.trim().length < 2) { setErr('grp-from','err-from','Enter a valid origin city (min 2 chars).'); ok=false; } else setOk('grp-from','err-from');
    if ($rTo.value.trim().length < 2)   { setErr('grp-to',  'err-to',  'Enter a valid destination city (min 2 chars).'); ok=false; } else setOk('grp-to','err-to');
    if ($rName.value.trim().length < 3) { setErr('grp-name','err-name','Enter a route name (min 3 chars).'); ok=false; } else setOk('grp-name','err-name');
    if (!$rFare.value || parseFloat($rFare.value) <= 0) { setErr('grp-fare','err-fare','Enter a valid fare amount.'); ok=false; } else setOk('grp-fare','err-fare');
    if (!$rDuration.value || parseInt($rDuration.value) < 1) { setErr('grp-duration','err-duration','Enter estimated travel time in minutes.'); ok=false; } else setOk('grp-duration','err-duration');
    return ok;
}

// ============================================================
// FORM SUBMIT (Add / Edit)
// ============================================================
const $routeForm = document.getElementById('routeForm');
$routeForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validateRouteForm()) { toast('Please fix the highlighted fields.', 'error'); return; }

    const btn = document.getElementById('route-submit-btn');
    btn.disabled = true;
    document.getElementById('route-btn-text').style.display   = 'none';
    document.getElementById('route-btn-loader').style.display = 'flex';

    const payload = {
        source:          $rFrom.value.trim(),
        destination:     $rTo.value.trim(),
        routeName:       $rName.value.trim(),
        fare:            $rFare.value,
        durationMinutes: $rDuration.value,
        distanceKm:      $rDistance.value || null,
    };

    const url    = editingId ? `/api/manage/routes/${editingId}` : '/api/manage/routes';
    const method = editingId ? 'PUT' : 'POST';

    try {
        const res  = await fetch(url, { method, headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) });
        const data = await res.json();
        if (!data.success) throw new Error(data.message);

        // Show success banner
        showBanner(
            editingId ? 'Route updated successfully!' : 'Route added successfully!',
            `Route ID: ${data.route.route_id}  |  ${data.route.source} → ${data.route.destination}`
        );
        toast(editingId ? `Route #${data.route.route_id} updated!` : `Route added — ID: ${data.route.route_id}`, 'success');
        cancelEdit();
        await loadHeroStats();

    } catch (err) {
        toast(err.message || 'Server error.', 'error');
    } finally {
        btn.disabled = false;
        document.getElementById('route-btn-text').style.display   = 'flex';
        document.getElementById('route-btn-loader').style.display = 'none';
    }
});

function showBanner(title, detail) {
    document.getElementById('rm-success-title').textContent  = title;
    document.getElementById('rm-success-detail').textContent = detail;
    document.getElementById('rm-success-banner').style.display = 'flex';
}
window.hideBanner = () => document.getElementById('rm-success-banner').style.display = 'none';

// ============================================================
// EDIT
// ============================================================
window.editRoute = function (id) {
    const route = allRoutes.find(r => r.route_id === id);
    if (!route) return;

    editingId = id;
    $rFrom.value     = route.source         || '';
    $rTo.value       = route.destination    || '';
    $rName.value     = route.route_name     || '';
    $rFare.value     = route.fare           || '';
    $rDuration.value = route.duration_minutes || '';
    $rDistance.value = route.distance_km    || '';
    document.getElementById('edit-route-id').value = id;

    // Update UI to Edit mode
    document.getElementById('form-title').textContent    = `Edit Route #${id}`;
    document.getElementById('form-subtitle').textContent = `Editing: ${route.source} → ${route.destination}`;
    document.getElementById('route-btn-text').innerHTML  = '<i class="fas fa-save"></i> Save Changes';
    document.getElementById('rm-cancel-btn').style.display = 'flex';
    document.getElementById('form-icon-el').innerHTML   = '<i class="fas fa-edit"></i>';
    hideBanner();
    updatePreview();
    rmTab('add');
    document.getElementById('routeForm').scrollIntoView({ behavior: 'smooth', block: 'start' });
};

window.cancelEdit = function () {
    editingId = null;
    $routeForm.reset();
    ['grp-from','grp-to','grp-name','grp-fare','grp-duration'].forEach(g => clrField(g, g.replace('grp-','err-')));
    document.getElementById('form-title').textContent    = 'Add New Route';
    document.getElementById('form-subtitle').textContent = 'Fill in the details and click Add Route';
    document.getElementById('route-btn-text').innerHTML  = '<i class="fas fa-plus-circle"></i> Add Route';
    document.getElementById('rm-cancel-btn').style.display = 'none';
    document.getElementById('form-icon-el').innerHTML   = '<i class="fas fa-map-marked-alt"></i>';
    updatePreview();
};

// ============================================================
// DELETE
// ============================================================
window.deleteRoute = function (id, name) {
    deleteTargetId = id;
    document.getElementById('rm-dialog-msg').textContent =
        `This will permanently remove "${name}" (ID: ${id}) from the database.`;
    document.getElementById('rm-dialog').style.display = 'flex';
};

window.closeDialog = function () {
    document.getElementById('rm-dialog').style.display = 'none';
    deleteTargetId = null;
};

document.getElementById('rm-dialog-confirm').addEventListener('click', async () => {
    if (!deleteTargetId) return;
    closeDialog();
    try {
        const res  = await fetch(`/api/manage/routes/${deleteTargetId}`, { method: 'DELETE' });
        const data = await res.json();
        if (!data.success) throw new Error(data.message);
        toast(data.message, 'success');
        await loadRoutesList();
        await loadHeroStats();
    } catch (err) {
        toast(err.message || 'Delete failed.', 'error');
    }
});

// ============================================================
// ROUTES LIST
// ============================================================
window.loadRoutesList = async function () {
    const tbody   = document.getElementById('routes-tbody');
    const countEl = document.getElementById('routes-count');
    tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row">
        <i class="fas fa-spinner fa-spin"></i><p>Loading from database…</p>
    </td></tr>`;

    try {
        const res  = await fetch('/api/manage/routes');
        const data = await res.json();
        allRoutes  = data.routes || [];

        if (allRoutes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row">
                <i class="fas fa-route" style="opacity:0.35"></i>
                <p>No routes found. Add your first route!</p>
            </td></tr>`;
            countEl.textContent = '0 routes';
            return;
        }

        renderTable(allRoutes);
        countEl.textContent = `${allRoutes.length} route${allRoutes.length!==1?'s':''} in the database`;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="appt-empty-row">
            <i class="fas fa-exclamation-triangle"></i>
            <p>Failed to load: ${escHtml(err.message)}</p>
        </td></tr>`;
    }
};

function renderTable(list) {
    const tbody = document.getElementById('routes-tbody');
    tbody.innerHTML = list.map(r => `
        <tr>
            <td><span class="rm-route-id-badge">#${r.route_id}</span></td>
            <td style="max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;"
                title="${escHtml(r.route_name||'')}">
                <strong>${escHtml(r.route_name || '—')}</strong>
            </td>
            <td><i class="fas fa-map-marker-alt" style="color:#4F46E5;margin-right:5px;"></i>${escHtml(r.source)}</td>
            <td><i class="fas fa-map-pin" style="color:#EC4899;margin-right:5px;"></i>${escHtml(r.destination)}</td>
            <td>₹ ${parseFloat(r.fare||0).toLocaleString('en-IN')}</td>
            <td>${r.distance_km ? r.distance_km+' km' : '—'}</td>
            <td>${r.duration_minutes ? minsToHrs(r.duration_minutes) : '—'}</td>
            <td>${formatDate(r.created_at)}</td>
            <td class="rm-actions-cell">
                <button class="rm-action-edit" onclick="editRoute(${r.route_id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="rm-action-del" onclick="deleteRoute(${r.route_id}, '${escHtml(r.route_name||r.source+' to '+r.destination)}')">
                    <i class="fas fa-trash-alt"></i> Delete
                </button>
            </td>
        </tr>
    `).join('');
}

window.filterTable = function (q) {
    if (!q.trim()) { renderTable(allRoutes); return; }
    const lq = q.toLowerCase();
    renderTable(allRoutes.filter(r =>
        (r.source||'').toLowerCase().includes(lq) ||
        (r.destination||'').toLowerCase().includes(lq) ||
        (r.route_name||'').toLowerCase().includes(lq) ||
        String(r.route_id).includes(lq)
    ));
};

// ============================================================
// BOOK VIA ROUTE
// ============================================================
async function loadBookRouteCards() {
    const sel = document.getElementById('rm-route-selector');
    sel.innerHTML = `<div style="text-align:center;padding:40px;color:#9CA3AF;grid-column:1/-1;">
        <i class="fas fa-spinner fa-spin" style="font-size:2rem;display:block;margin-bottom:12px;"></i>
        <p>Loading routes…</p>
    </div>`;
    document.getElementById('rm-booking-panel').style.display = 'none';

    try {
        const res  = await fetch('/api/manage/routes');
        const data = await res.json();
        const list = data.routes || [];
        if (list.length === 0) {
            sel.innerHTML = `<div style="text-align:center;padding:40px;color:#9CA3AF;grid-column:1/-1;">
                <i class="fas fa-route" style="font-size:2rem;display:block;margin-bottom:12px;opacity:0.4"></i>
                <p>No routes available. Add routes first.</p>
            </div>`;
            return;
        }
        sel.innerHTML = list.map(r => `
            <div class="rm-route-card" id="rc-${r.route_id}" onclick="selectRouteCard(${r.route_id})">
                <span class="rm-select-badge"><i class="fas fa-check"></i> Selected</span>
                <div class="rm-rc-header">
                    <span class="rm-rc-id">ID: ${r.route_id}</span>
                    <span class="rm-rc-name" title="${escHtml(r.route_name||'')}">
                        ${escHtml(r.route_name || r.source+' → '+r.destination)}
                    </span>
                </div>
                <div class="rm-rc-route">
                    <span class="rm-rc-city">${escHtml(r.source)}</span>
                    <div class="rm-rc-arrow"><i class="fas fa-arrow-right"></i></div>
                    <span class="rm-rc-city">${escHtml(r.destination)}</span>
                </div>
                <div class="rm-rc-meta">
                    <span><i class="fas fa-rupee-sign"></i> ₹${parseFloat(r.fare||0).toLocaleString('en-IN')}</span>
                    <span><i class="fas fa-clock"></i> ${minsToHrs(r.duration_minutes)}</span>
                    ${r.distance_km ? `<span><i class="fas fa-road"></i> ${r.distance_km} km</span>` : ''}
                </div>
            </div>
        `).join('');
    } catch (err) {
        sel.innerHTML = `<div style="text-align:center;padding:40px;color:#EF4444;grid-column:1/-1;">
            <p>Failed to load routes: ${escHtml(err.message)}</p>
        </div>`;
    }
}

window.selectRouteCard = function (routeId) {
    // Deselect all
    document.querySelectorAll('.rm-route-card').forEach(c => c.classList.remove('selected'));
    document.getElementById(`rc-${routeId}`).classList.add('selected');

    const route  = (allRoutes.length ? allRoutes : []).find(r => r.route_id === routeId);
    const label  = route ? `${route.source} → ${route.destination}` : `Route #${routeId}`;
    const rName  = route?.route_name || label;

    // Populate hidden fields
    document.getElementById('bvr-route-id').value    = routeId;
    document.getElementById('bvr-route-label').value = label;

    // Banner
    document.getElementById('rm-selected-route-banner').innerHTML = `
        <i class="fas fa-route" style="font-size:1.3rem;flex-shrink:0;"></i>
        <div>
            <strong>${escHtml(rName)}</strong>
            <span style="display:block;font-size:0.8rem;opacity:0.8;margin-top:2px;">
                Route ID: ${routeId} | ${escHtml(label)}
            </span>
        </div>
    `;

    // Show panel & build slot grid
    const panel = document.getElementById('rm-booking-panel');
    panel.style.display = 'grid';
    buildBvrSlots();
    setDateMin();
    panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
};

function setDateMin() {
    const today = new Date();
    const ymd = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;
    document.getElementById('bvr-date').min = ymd;
}

function buildBvrSlots(taken = []) {
    const grid = document.getElementById('bvr-slot-grid');
    grid.innerHTML = '';
    SLOTS.forEach(s => {
        const btn = document.createElement('button');
        btn.type = 'button';
        const isTaken = taken.includes(s.label);
        btn.className = 'slot-btn' + (isTaken ? ' slot-taken' : '');
        btn.dataset.slot = s.label;
        btn.innerHTML = `${s.label}<small>${s.sub}</small>`;
        if (!isTaken) {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#bvr-slot-grid .slot-btn').forEach(b => b.classList.remove('slot-selected'));
                btn.classList.add('slot-selected');
                document.getElementById('bvr-slot').value = s.label;
                clrBvrErr('bvr-grp-slot', 'bvr-err-slot');
            });
        }
        grid.appendChild(btn);
    });
}

document.getElementById('bvr-date').addEventListener('change', async function () {
    const date = this.value;
    if (!date) return;
    try {
        const res  = await fetch(`/api/appointments/taken-slots?date=${encodeURIComponent(date)}`);
        const data = await res.json();
        document.getElementById('bvr-slot').value = '';
        buildBvrSlots(data.takenSlots || []);
    } catch (_) { buildBvrSlots(); }
});

// ---- BVR Validation helpers ----
function setBvrErr(grp, errId, msg) {
    document.getElementById(grp).classList.add('has-error');
    document.getElementById(errId).textContent = msg;
}
function clrBvrErr(grp, errId) {
    document.getElementById(grp).classList.remove('has-error','has-success');
    document.getElementById(errId).textContent = '';
}

function validateBvrForm() {
    let ok = true;
    const n = document.getElementById('bvr-name').value.trim();
    const e = document.getElementById('bvr-email').value.trim();
    const p = document.getElementById('bvr-phone').value.trim();
    const d = document.getElementById('bvr-date').value;
    const s = document.getElementById('bvr-slot').value;

    if (n.length < 2) { setBvrErr('bvr-grp-name','bvr-err-name','Enter full name (min 2 chars).'); ok=false; }
    else { clrBvrErr('bvr-grp-name','bvr-err-name'); }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)) { setBvrErr('bvr-grp-email','bvr-err-email','Enter a valid email.'); ok=false; }
    else { clrBvrErr('bvr-grp-email','bvr-err-email'); }

    if (!/^[6-9]\d{9}$/.test(p)) { setBvrErr('bvr-grp-phone','bvr-err-phone','Enter a valid 10-digit Indian number.'); ok=false; }
    else { clrBvrErr('bvr-grp-phone','bvr-err-phone'); }

    if (!d) { setBvrErr('bvr-grp-date','bvr-err-date','Select a travel date.'); ok=false; }
    else { clrBvrErr('bvr-grp-date','bvr-err-date'); }

    if (!s) { setBvrErr('bvr-grp-slot','bvr-err-slot','Select a time slot.'); ok=false; }
    else { clrBvrErr('bvr-grp-slot','bvr-err-slot'); }

    return ok;
}

document.getElementById('bookingViaRouteForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validateBvrForm()) { toast('Please fix all fields.', 'error'); return; }

    const btn = document.getElementById('bvr-submit-btn');
    btn.disabled = true;
    document.getElementById('bvr-btn-text').style.display   = 'none';
    document.getElementById('bvr-btn-loader').style.display = 'flex';

    const routeLabel = document.getElementById('bvr-route-label').value;
    const payload = {
        name:     document.getElementById('bvr-name').value.trim(),
        email:    document.getElementById('bvr-email').value.trim(),
        phone:    document.getElementById('bvr-phone').value.trim(),
        date:     document.getElementById('bvr-date').value,
        timeSlot: document.getElementById('bvr-slot').value,
    };

    try {
        const res  = await fetch('/api/appointments/book', {
            method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (!data.success) throw new Error(data.message);

        const bk = data.booking;
        document.getElementById('bvr-preview-id').textContent    = bk.bookingId;
        document.getElementById('bvr-preview-route').textContent = routeLabel;
        document.getElementById('bvr-preview-name').textContent  = bk.name;
        document.getElementById('bvr-preview-email').textContent = bk.email;
        document.getElementById('bvr-preview-date').textContent  = fmtDate(bk.date);
        document.getElementById('bvr-preview-slot').textContent  = bk.timeSlot;

        document.getElementById('bookingViaRouteForm').style.display = 'none';
        const pv = document.getElementById('bvr-preview');
        pv.style.display = 'block';
        pv.classList.add('visible');
        pv.scrollIntoView({ behavior:'smooth', block:'nearest' });
        toast(`Booking confirmed! ID: ${bk.bookingId}`, 'success');

    } catch (err) {
        toast(err.message || 'Booking failed.', 'error');
    } finally {
        btn.disabled = false;
        document.getElementById('bvr-btn-text').style.display   = 'flex';
        document.getElementById('bvr-btn-loader').style.display = 'none';
    }
});

window.resetBvrForm = function () {
    document.getElementById('bookingViaRouteForm').reset();
    document.getElementById('bookingViaRouteForm').style.display = 'block';
    const pv = document.getElementById('bvr-preview');
    pv.style.display = 'none';
    pv.classList.remove('visible');
    document.getElementById('bvr-slot').value = '';
    buildBvrSlots();
    document.querySelectorAll('.rm-route-card').forEach(c => c.classList.remove('selected'));
    document.getElementById('rm-booking-panel').style.display = 'none';
};

// ============================================================
// HELPERS
// ============================================================
function escHtml(s) {
    return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}
function formatDate(ts) {
    if (!ts) return '—';
    return new Date(ts).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
}
function fmtDate(raw) {
    if (!raw) return '--';
    return new Date(raw).toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' });
}

// ============================================================
// INIT
// ============================================================
(async function init() {
    updatePreview();
    await loadHeroStats();
    // Pre-load routes into allRoutes for edit lookups
    try {
        const res  = await fetch('/api/manage/routes');
        const data = await res.json();
        allRoutes  = data.routes || [];
    } catch (_) {}
})();
