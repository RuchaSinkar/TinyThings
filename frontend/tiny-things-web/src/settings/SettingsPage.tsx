import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import client from '../api/client';

const INTEREST_OPTIONS = [
  'Coding', 'Fitness', 'Music', 'Reading', 'Art', 'Gaming',
  'Cooking', 'Writing', 'Travel', 'Sports',
];

export function SettingsPage() {
  const navigate = useNavigate();
  const [interests, setInterests] = useState<string[]>([]);
  const [customInput, setCustomInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadProfile();
  }, []);

  async function loadProfile() {
    try {
      const res = await client.get('/api/profile/me');
      setInterests(res.data.interests ?? []);
    } catch {
      setError('Could not load your profile.');
    } finally {
      setLoading(false);
    }
  }

  function toggle(option: string) {
    const lower = option.toLowerCase();
    const exists = interests.some((i) => i.toLowerCase() === lower);
    setInterests((prev) =>
      exists ? prev.filter((i) => i.toLowerCase() !== lower) : [...prev, option]
    );
    setSaved(false);
  }

  function addCustom() {
    const trimmed = customInput.trim();
    if (!trimmed) return;
    const lower = trimmed.toLowerCase();
    const exists = interests.some((i) => i.toLowerCase() === lower);
    if (!exists) {
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
      await client.patch('/api/profile/interests', { interests });
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

  // Custom = anything in `interests` that isn't one of the preset options
  const presetLower = INTEREST_OPTIONS.map((o) => o.toLowerCase());
  const customTags = interests.filter((i) => !presetLower.includes(i.toLowerCase()));

  return (
    <div className="flex min-h-screen flex-col items-center bg-slate-900 px-4 py-10">
      <div className="w-full max-w-sm">
        <button
          onClick={() => navigate('/')}
          className="text-sm text-slate-500 hover:text-slate-300"
        >
          ← Back
        </button>

        <h1 className="mt-4 text-xl font-semibold text-white">Your Interests</h1>
        <p className="mt-1 text-sm text-slate-400">
          This shapes the Tiny Things you get suggested.
        </p>

        <div className="mt-5 flex flex-wrap gap-2">
          {INTEREST_OPTIONS.map((opt) => {
            const isSelected = interests.some((i) => i.toLowerCase() === opt.toLowerCase());
            return (
              <button
                key={opt}
                onClick={() => toggle(opt)}
                className={`rounded-full px-3.5 py-1.5 text-sm transition ${
                  isSelected
                    ? 'bg-indigo-500 text-white'
                    : 'bg-slate-700 text-slate-200 hover:bg-slate-600'
                }`}
              >
                {opt}
              </button>
            );
          })}
        </div>

        {/* Custom interests */}
        <div className="mt-5">
          <span className="text-xs uppercase tracking-wide text-slate-500">
            Anything else?
          </span>

          {customTags.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-2">
              {customTags.map((tag) => (
                <span
                  key={tag}
                  className="flex items-center gap-1.5 rounded-full bg-indigo-500 px-3.5 py-1.5 text-sm text-white"
                >
                  {tag}
                  <button
                    onClick={() => removeCustom(tag)}
                    className="text-white/70 hover:text-white"
                  >
                    ✕
                  </button>
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
              placeholder="Type your own interest..."
              className="flex-1 rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
            />
            <button
              onClick={addCustom}
              className="rounded-lg bg-slate-600 px-3 py-2 text-sm text-white hover:bg-slate-500"
            >
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