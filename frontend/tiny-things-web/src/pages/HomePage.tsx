import { useState, useEffect } from 'react';
import { useAuthStore } from '../auth/authStore';
import client from '../api/client';
import { useNavigate } from 'react-router-dom';

interface TinyThing {
  historyId: string;
  id: string;
  title: string;
  description: string;
  category: string;
  tags: string[];
}

interface HydrationData {
  date: string;
  slotCount: number;
  maxSlots: number;
}

interface StreakData {
  currentStreak: number;
  longestStreak: number;
}

interface GoalData {
  id: string;
  title: string;
  completed: boolean;
  subtasks: GoalData[];
}

interface GratitudeEntryData {
  id: string;
  entryType: string;
  content: string | null;
  completedAt: string;
}

function todayStr() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}
const ROBOT_MOODS: Record<string, { emoji: string; message: string }> = {
  idle: { emoji: '🤖', message: "Ready when you are." },
  celebrating: { emoji: '🎉', message: "Yes! Nice work." },
  sad: { emoji: '😴', message: "I haven't seen you in a while..." },
};

function RobotDisplay({ mood }: { mood: string }) {
  const { emoji, message } = ROBOT_MOODS[mood] ?? ROBOT_MOODS.idle;
  return (
    <div className="flex flex-col items-center">
      <div className="text-6xl transition-transform duration-300 hover:scale-110">{emoji}</div>
      <p className="mt-2 text-sm text-slate-400">{message}</p>
    </div>
  );
}


export function HomePage() {
  const logout = useAuthStore((s) => s.logout);
  const [thing, setThing] = useState<TinyThing | null>(null);
  const [loading, setLoading] = useState(false);
  const [completed, setCompleted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [todayCount, setTodayCount] = useState(0);

  const [hydration, setHydration] = useState<HydrationData | null>(null);
  const [streak, setStreak] = useState<StreakData | null>(null);

  const [goals, setGoals] = useState<GoalData[]>([]);
  const [newGoalTitle, setNewGoalTitle] = useState('');
  const [newSubtaskFor, setNewSubtaskFor] = useState<string | null>(null);
  const [newSubtaskTitle, setNewSubtaskTitle] = useState('');

  const [gratitudeToday, setGratitudeToday] = useState<GratitudeEntryData[]>([]);
  const [gratitudeNote, setGratitudeNote] = useState('');
  const [showGratitudeInput, setShowGratitudeInput] = useState<string | null>(null);

  const [robotMood, setRobotMood] = useState('idle');

  const navigate = useNavigate();

  useEffect(() => {
    // loadHydration();
    // loadStreak();
    // loadGoals();
    // loadGratitude();
    loadSummary();
  }, []);


  useEffect(() => {
    if (completed) {
      const timer = setTimeout(() => {
        setThing(null);
        setCompleted(false);
      }, 2000);
      return () => clearTimeout(timer);
    }
  }, [completed]);

  async function loadSummary() {
  try {
    const res = await client.get('/api/home-summary', { params: { date: todayStr() } });
    setRobotMood(res.data.robotMood);
    setHydration({ date: todayStr(), slotCount: res.data.hydrationSlotCount, maxSlots: res.data.hydrationMaxSlots });
    setStreak({ currentStreak: res.data.currentStreak, longestStreak: res.data.longestStreak });
    setGoals(res.data.goals);
  } catch {
    // silent
  }
}
  async function loadHydration() {
    try {
      const res = await client.get<HydrationData>('/api/hydration', {
        params: { date: todayStr() },
      });
      setHydration(res.data);
    } catch {
      // silent — non-critical for page load
    }
  }

  async function loadStreak() {
    try {
      const res = await client.get<StreakData>('/api/streak');
      setStreak(res.data);
    } catch {
      // silent
    }
  }

  async function handleAddSlot() {
    try {
      const res = await client.post<HydrationData>('/api/hydration/add', null, {
        params: { date: todayStr() },
      });
      setHydration(res.data);
      loadStreak(); // hydration can bump the streak, refresh it
    } catch {
      setError('Could not log water. Try again?');
    }
  }

  async function loadGoals() {
  try {
    const res = await client.get<GoalData[]>('/api/goals');
    setGoals(res.data);
  } catch {
    // silent
  }
}

async function loadGratitude() {
  try {
    const res = await client.get<GratitudeEntryData[]>('/api/gratitude');
    setGratitudeToday(res.data);
  } catch {
    // silent
  }
}

async function handleCreateGoal() {
  if (!newGoalTitle.trim()) return;
  try {
    await client.post('/api/goals', { title: newGoalTitle.trim(), parentGoalId: null });
    setNewGoalTitle('');
    loadGoals();
  } catch {
    setError('Could not create goal. Try again?');
  }
}

async function handleCreateSubtask(parentId: string) {
  if (!newSubtaskTitle.trim()) return;
  try {
    await client.post('/api/goals', { title: newSubtaskTitle.trim(), parentGoalId: parentId });
    setNewSubtaskTitle('');
    setNewSubtaskFor(null);
    loadGoals();
  } catch {
    setError('Could not add subtask. Try again?');
  }
}

async function handleCompleteGoal(goalId: string) {
  try {
    await client.patch(`/api/goals/${goalId}/complete`);
    loadGoals();
    loadStreak();
  } catch {
    setError('Could not update goal. Try again?');
  }
}

  async function handleGratitudeAction(entryType: string, content: string | null) {
    try {
      await client.post('/api/gratitude', { entryType, content });
      setGratitudeNote('');
      setShowGratitudeInput(null);
      loadGratitude();
      loadStreak();
    } catch {
      setError('Could not log that. Try again?');
    }
  }

  async function handleRemoveSlot() {
    try {
      const res = await client.post<HydrationData>('/api/hydration/remove', null, {
        params: { date: todayStr() },
      });
      setHydration(res.data);
    } catch {
      setError('Could not update. Try again?');
    }
  }

  async function handleSurpriseMe() {
    setLoading(true);
    setError(null);
    setCompleted(false);
    try {
      const res = await client.get<TinyThing>('/api/tiny-things/surprise');
      setThing(res.data);
    } catch {
      setError('Could not get a Tiny Thing right now. Try again?');
    } finally {
      setLoading(false);
    }
  }

  async function handleMarkComplete() {
    if (!thing) return;
    try {
      await client.patch(`/api/tiny-things/history/${thing.historyId}/complete`);
      setCompleted(true);
      setTodayCount((c) => c + 1);
      loadStreak();
    } catch {
      setError('Could not mark that complete. Try again?');
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center bg-slate-900 px-4 py-10">
      <h1 className="text-2xl font-semibold text-white">Tiny Things</h1>
          <button
      onClick={() => navigate('/settings')}
      className="mt-2 text-xs text-slate-500 hover:text-slate-300"
    > ⚙️ Edit interests </button>
      <p className="mt-1 text-sm text-slate-400">One small thing at a time.</p>

         <RobotDisplay mood={robotMood} />

      {streak && streak.currentStreak > 0 && (
        <div className="mt-3 flex items-center gap-1.5 rounded-full bg-orange-500/10 px-3 py-1">
          <span className="text-sm">🔥</span>
          <span className="text-sm font-medium text-orange-400">
            {streak.currentStreak} day{streak.currentStreak !== 1 ? 's' : ''} streak
          </span>
        </div>
      )}

      {/* Hydration tracker */}
      {hydration && (
        <div className="mt-6 w-full max-w-sm rounded-2xl bg-slate-800 p-5">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-white">💧 Water today</span>
            <span className="text-sm text-slate-400">
              {hydration.slotCount}/{hydration.maxSlots}
            </span>
          </div>
          <div className="mt-3 flex gap-1.5">
            {Array.from({ length: hydration.maxSlots }).map((_, i) => (
              <div
                key={i}
                className={`h-8 flex-1 rounded-md transition-colors ${
                  i < hydration.slotCount ? 'bg-sky-500' : 'bg-slate-700'
                }`}
              />
            ))}
          </div>
          <button
            onClick={handleAddSlot}
            disabled={hydration.slotCount >= hydration.maxSlots}
            className="mt-3 w-full rounded-lg bg-sky-500/20 py-2 text-sm font-medium text-sky-400 hover:bg-sky-500/30 disabled:opacity-40"
          >
            {hydration.slotCount >= hydration.maxSlots ? 'All done for today 🎉' : '+ Log a glass'}
          </button>
        </div>
      )}

      {/* Daily Goals */}
<div className="mt-6 w-full max-w-sm rounded-2xl bg-slate-800 p-5">
  <span className="text-sm font-medium text-white">🎯 Today's Goals</span>

  <div className="mt-3 space-y-2">
    {goals.map((goal) => (
      <div key={goal.id}>
        <div className="flex items-center gap-2">
          <button
            onClick={() => !goal.completed && handleCompleteGoal(goal.id)}
            className={`h-5 w-5 shrink-0 rounded border-2 flex items-center justify-center ${
              goal.completed
                ? 'border-emerald-500 bg-emerald-500'
                : 'border-slate-600 hover:border-slate-400'
            }`}
          >
            {goal.completed && <span className="text-xs leading-none text-white">✓</span>}
          </button>
          <span className={`text-sm ${goal.completed ? 'text-slate-500 line-through' : 'text-slate-200'}`}>
            {goal.title}
          </span>
          {!goal.completed && (
            <button
              onClick={() => setNewSubtaskFor(newSubtaskFor === goal.id ? null : goal.id)}
              className="ml-auto text-xs text-slate-500 hover:text-slate-300"
            >
              + subtask
            </button>
          )}
        </div>

        {goal.subtasks.length > 0 && (
          <div className="ml-7 mt-1.5 space-y-1.5">
            {goal.subtasks.map((sub) => (
              <div key={sub.id} className="flex items-center gap-2">
                <button
                  onClick={() => !sub.completed && handleCompleteGoal(sub.id)}
                  className={`h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center ${
                    sub.completed
                      ? 'border-emerald-500 bg-emerald-500'
                      : 'border-slate-600 hover:border-slate-400'
                  }`}
                >
                  {sub.completed && <span className="text-[10px] leading-none text-white">✓</span>}
                </button>
                <span className={`text-xs ${sub.completed ? 'text-slate-500 line-through' : 'text-slate-300'}`}>
                  {sub.title}
                </span>
              </div>
            ))}
          </div>
        )}

        {newSubtaskFor === goal.id && (
          <div className="ml-7 mt-1.5 flex gap-1.5">
            <input
              autoFocus
              type="text"
              value={newSubtaskTitle}
              onChange={(e) => setNewSubtaskTitle(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCreateSubtask(goal.id)}
              placeholder="Subtask..."
              className="flex-1 rounded bg-slate-700 px-2 py-1 text-xs text-white placeholder-slate-500"
            />
            <button
              onClick={() => handleCreateSubtask(goal.id)}
              className="rounded bg-indigo-500 px-2 py-1 text-xs text-white hover:bg-indigo-600"
            >
              Add
            </button>
          </div>
        )}
      </div>
    ))}
  </div>

  <div className="mt-3 flex gap-1.5">
    <input
      type="text"
      value={newGoalTitle}
      onChange={(e) => setNewGoalTitle(e.target.value)}
      onKeyDown={(e) => e.key === 'Enter' && handleCreateGoal()}
      placeholder="Add a goal for today..."
      className="flex-1 rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
    />
    <button
      onClick={handleCreateGoal}
      className="rounded-lg bg-indigo-500 px-3 py-2 text-sm text-white hover:bg-indigo-600"
    >
      Add
    </button>
  </div>
</div>

{/* Gratitude */}
  <div className="mt-6 w-full max-w-sm rounded-2xl bg-slate-800 p-5">
    <span className="text-sm font-medium text-white">🙏 Gratitude & Connection</span>

    <div className="mt-3 flex flex-wrap gap-2">
      <button
        onClick={() => setShowGratitudeInput(showGratitudeInput === 'gratitude' ? null : 'gratitude')}
        className="rounded-full bg-slate-700 px-3 py-1.5 text-xs text-slate-200 hover:bg-slate-600"
      >
        Grateful for...
      </button>
      <button
        onClick={() => handleGratitudeAction('thank_someone', null)}
        className="rounded-full bg-slate-700 px-3 py-1.5 text-xs text-slate-200 hover:bg-slate-600"
      >
        Thanked someone
      </button>
      <button
        onClick={() => handleGratitudeAction('message_someone', null)}
        className="rounded-full bg-slate-700 px-3 py-1.5 text-xs text-slate-200 hover:bg-slate-600"
      >
        Messaged someone
      </button>
    </div>

    {showGratitudeInput === 'gratitude' && (
      <div className="mt-2 flex gap-1.5">
        <input
          autoFocus
          type="text"
          value={gratitudeNote}
          onChange={(e) => setGratitudeNote(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleGratitudeAction('gratitude', gratitudeNote)}
          placeholder="I'm grateful for..."
          className="flex-1 rounded-lg bg-slate-700 px-3 py-2 text-sm text-white placeholder-slate-500"
        />
        <button
          onClick={() => handleGratitudeAction('gratitude', gratitudeNote)}
          className="rounded-lg bg-indigo-500 px-3 py-2 text-sm text-white hover:bg-indigo-600"
        >
          Save
        </button>
      </div>
    )}

    {gratitudeToday.length > 0 && (
      <p className="mt-3 text-xs text-slate-500">
        {gratitudeToday.length} logged today
      </p>
    )}
  </div>

      {/* Tiny Things */}
      {todayCount > 0 && (
        <p className="mt-6 text-sm text-indigo-400">
          {todayCount} Tiny {todayCount === 1 ? 'Thing' : 'Things'} today ✨
        </p>
      )}

      <div className="mt-4 w-full max-w-sm">
        {!thing && (
          <button
            onClick={handleSurpriseMe}
            disabled={loading}
            className="w-full rounded-xl bg-indigo-500 py-4 text-lg font-medium text-white hover:bg-indigo-600 disabled:opacity-50"
          >
            {loading ? 'Finding something...' : '✨ Surprise Me'}
          </button>
        )}

        {thing && (
          <div
            className={`rounded-2xl bg-slate-800 p-6 shadow-xl transition-opacity duration-500 ${
              completed ? 'opacity-60' : 'opacity-100'
            }`}
          >
            {!completed ? (
              <>
                <span className="text-xs uppercase tracking-wide text-indigo-400">
                  {thing.category}
                </span>
                <h2 className="mt-2 text-lg font-semibold text-white">{thing.title}</h2>
                <p className="mt-2 text-sm text-slate-400">{thing.description}</p>

                {thing.tags.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-1.5">
                    {thing.tags.map((tag) => (
                      <span
                        key={tag}
                        className="rounded-full bg-slate-700 px-2.5 py-0.5 text-xs text-slate-300"
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                )}

                <div className="mt-5 flex gap-2">
                  <button
                    onClick={handleMarkComplete}
                    className="flex-1 rounded-lg bg-emerald-500 py-2.5 text-sm font-medium text-white hover:bg-emerald-600"
                  >
                    ✓ Done
                  </button>
                  <button
                    onClick={handleSurpriseMe}
                    className="rounded-lg bg-slate-700 px-4 py-2.5 text-sm text-slate-300 hover:bg-slate-600"
                  >
                    Skip
                  </button>
                </div>
              </>
            ) : (
              <div className="py-4 text-center">
                <p className="text-lg font-medium text-emerald-400">Nice work! 🎉</p>
                <p className="mt-1 text-sm text-slate-500">Come back anytime for another.</p>
              </div>
            )}
          </div>
        )}

        {error && <p className="mt-3 text-center text-sm text-red-400">{error}</p>}
      </div>

      <button
        onClick={logout}
        className="mt-10 text-sm text-slate-500 hover:text-slate-300"
      >
        Log out
      </button>
    </div>
  );
}