import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import client from '../api/client';

const INTEREST_OPTIONS = [
  'Coding', 'Fitness', 'Music', 'Reading', 'Art', 'Gaming',
  'Cooking', 'Writing', 'Travel', 'Sports',
];

const AVATAR_OPTIONS: { id: string; emoji: string; label: string }[] = [
  { id: 'robot', emoji: '🤖', label: 'Robot' },
  { id: 'cat', emoji: '🐱', label: 'Cat' },
  { id: 'fox', emoji: '🦊', label: 'Fox' },
  { id: 'alien', emoji: '👾', label: 'Alien' },
  { id: 'ghost', emoji: '👻', label: 'Ghost' },
];

export function SettingsPage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [interests, setInterests] = useState<string[]>([]);
  const [customInput, setCustomInput] = useState('');
  const [avatarId, setAvatarId] = useState('robot');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [field, setField] = useState('');
  const [goalsText, setGoalsText] = useState('');

  useEffect(() => {
    loadProfile();
  }, []);

  async function loadProfile() {
    try {
      const [profileRes, summaryRes] = await Promise.all([
        client.get('/api/profile/me'),
        client.get('/api/home-summary', { params: { date: new Date().toISOString().slice(0, 10) } }),
      ]);
      setName(profileRes.data.name ?? '');
      setInterests(profileRes.data.interests ?? []);
      setAvatarId(summaryRes.data.robotAvatarId ?? 'robot');
      setField(profileRes.data.field ?? '');
      setGoalsText(profileRes.data.goalsText ?? '');
    } catch {
      setError('Could not load your profile.');
    } finally {
      setLoading(false);
    }
  }

  function toggleInterest(option: string) {
    const lower = option.toLowerCase();
    const exists = interests.some((i) => i.toLowerCase() === lower);
    setInterests((prev) => (exists ? prev.filter((i) => i.toLowerCase() !== lower) : [...prev, option]));
    setSaved(false);
  }

  function addCustom() {
    const trimmed = customInput.trim();
    if (!trimmed) return;
    const lower = trimmed.toLowerCase();
    if (!interests.some((i) => i.toLowerCase() === lower)) {
      setInterests((prev) => [...prev, trimmed]);
      setSaved(false);
    }
    setCustomInput('');
  }

  function removeCustom(tag: string) {
    setInterests((prev) => prev.filter((i) => i.toLowerCase() !== tag.toLowerCase()));
    setSaved(false);
  }

  async function handleSave() {
    setSaving(true);
    setError(null);
    try {
      await client.patch('/api/profile', { name, interests, field, goalsText });
      await client.patch('/api/robot/avatar', { avatarId });
      setSaved(true);
    } catch {
      setError('Could not save. Try again?');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-900">
        <p className="text-slate-400">Loading...</p>
      </div>
    );
  }

  const presetLower = INTEREST_OPTIONS.map((o) => o.toLowerCase());
  const customTags = interests.filter((i) => !presetLower.includes(i.toLowerCase()));

  return (
    <div className="flex min-h-screen flex-col items-center bg-slate-900 px-4 py-10">
      <div className="w-full max-w-sm">
        <button onClick={() => navigate('/')} className="text-sm text-slate-500 hover:text-slate-300">
          ← Back
        </button>

        <h1 className="mt-4 text-xl font-semibold text-white">Settings</h1>

        {/* Name */}
        <div className="mt-5">
          <span className="text-xs uppercase tracking-wide text-slate-500">What should we call you?</span>
          <input
            type="text"
            value={name}
            onChange={(e) => { setName(e.target.value); setSaved(false); }}
            className="mt-2 w-full rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
            placeholder="Your name"
          />
          <div className="mt-6">
            <span className="text-xs uppercase tracking-wide text-slate-500">Field / focus</span>
            <input
              type="text"
              value={field}
              onChange={(e) => { setField(e.target.value); setSaved(false); }}
              className="mt-2 w-full rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
              placeholder="e.g. Computer Science, Design, Fitness"
            />
          </div>

          <div className="mt-6">
            <span className="text-xs uppercase tracking-wide text-slate-500">Current goal</span>
            <textarea
              value={goalsText}
              onChange={(e) => { setGoalsText(e.target.value); setSaved(false); }}
              rows={2}
              className="mt-2 w-full rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
              placeholder="What are you working toward?"
            />
          </div>
        </div>

        {/* Robot avatar */}
        <div className="mt-6">
          <span className="text-xs uppercase tracking-wide text-slate-500">Robot avatar</span>
          <div className="mt-2 flex gap-2">
            {AVATAR_OPTIONS.map((opt) => (
              <button
                key={opt.id}
                onClick={() => { setAvatarId(opt.id); setSaved(false); }}
                className={`flex h-14 w-14 flex-col items-center justify-center rounded-xl text-2xl transition ${
                  avatarId === opt.id ? 'bg-indigo-500' : 'bg-slate-700 hover:bg-slate-600'
                }`}
                title={opt.label}
              >
                {opt.emoji}
              </button>
            ))}
          </div>
        </div>

        {/* Interests */}
        <div className="mt-6">
          <span className="text-xs uppercase tracking-wide text-slate-500">Your interests</span>
          <div className="mt-2 flex flex-wrap gap-2">
            {INTEREST_OPTIONS.map((opt) => {
              const isSelected = interests.some((i) => i.toLowerCase() === opt.toLowerCase());
              return (
                <button
                  key={opt}
                  onClick={() => toggleInterest(opt)}
                  className={`rounded-full px-3.5 py-1.5 text-sm transition ${
                    isSelected ? 'bg-indigo-500 text-white' : 'bg-slate-700 text-slate-200 hover:bg-slate-600'
                  }`}
                >
                  {opt}
                </button>
              );
            })}
          </div>

          {customTags.length > 0 && (
            <div className="mt-3 flex flex-wrap gap-2">
              {customTags.map((tag) => (
                <span key={tag} className="flex items-center gap-1.5 rounded-full bg-indigo-500 px-3.5 py-1.5 text-sm text-white">
                  {tag}
                  <button onClick={() => removeCustom(tag)} className="text-white/70 hover:text-white">✕</button>
                </span>
              ))}
            </div>
          )}

          <div className="mt-2 flex gap-1.5">
            <input
              type="text"
              value={customInput}
              onChange={(e) => setCustomInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && addCustom()}
              placeholder="Add your own..."
              className="flex-1 rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
            />
            <button onClick={addCustom} className="rounded-lg bg-slate-600 px-3 py-2 text-sm text-white hover:bg-slate-500">
              Add
            </button>
          </div>
        </div>

        {error && <p className="mt-4 text-sm text-red-400">{error}</p>}

        <button
          onClick={handleSave}
          disabled={saving}
          className="mt-6 w-full rounded-lg bg-indigo-500 py-2.5 text-sm font-medium text-white hover:bg-indigo-600 disabled:opacity-50"
        >
          {saving ? 'Saving...' : saved ? 'Saved ✓' : 'Save changes'}
        </button>
      </div>
    </div>
  );
}