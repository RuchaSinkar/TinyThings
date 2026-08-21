import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../auth/authStore';
import client from '../api/client';

interface TinyThing {
  historyId: string;
  id: string;
  title: string;
  description: string;
  category: string;
  tags: string[];
}

interface HydrationData {
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

function todayStr() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

const ROBOT_AVATARS: Record<string, string> = {
  robot: '🤖',
  cat: '🐱',
  fox: '🦊',
  alien: '👾',
  ghost: '👻',
};

const MOOD_OVERRIDE: Record<string, { emoji: string; message: string }> = {
  celebrating: { emoji: '🎉', message: "Yes! Nice work." },
  sad: { emoji: '😴', message: "I haven't seen you in a while..." },
};

function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

export function HomePage() {
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);

  const [userName, setUserName] = useState('');
  const [robotMood, setRobotMood] = useState('idle');
  const [avatarId, setAvatarId] = useState('robot');

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

  useEffect(() => {
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
      setUserName(res.data.userName ?? '');
      setRobotMood(res.data.robotMood);
      setAvatarId(res.data.robotAvatarId ?? 'robot');
      setHydration({ slotCount: res.data.hydrationSlotCount, maxSlots: res.data.hydrationMaxSlots });
      setStreak({ currentStreak: res.data.currentStreak, longestStreak: res.data.longestStreak });
      setGoals(res.data.goals);
      setTodayCount(res.data.tinyThingsCompletedToday);
    } catch {
      // silent
    }
  }

  async function handleAddSlot() {
    try {
      await client.post('/api/hydration/add', null, { params: { date: todayStr() } });
      loadSummary();
    } catch {
      setError('Could not log water. Try again?');
    }
  }

  async function handleCreateGoal() {
    if (!newGoalTitle.trim()) return;
    try {
      await client.post('/api/goals', { title: newGoalTitle.trim(), parentGoalId: null });
      setNewGoalTitle('');
      loadSummary();
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
      loadSummary();
    } catch {
      setError('Could not add subtask. Try again?');
    }
  }

  async function handleCompleteGoal(goalId: string) {
    try {
      await client.patch(`/api/goals/${goalId}/complete`);
      loadSummary();
    } catch {
      setError('Could not update goal. Try again?');
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
      loadSummary();
    } catch {
      setError('Could not mark that complete. Try again?');
    }
  }

  const moodDisplay = MOOD_OVERRIDE[robotMood] ?? {
    emoji: ROBOT_AVATARS[avatarId] ?? '🤖',
    message: 'Ready when you are.',
  };

  return (
    <div className="flex min-h-screen flex-col items-center bg-slate-900 px-4 py-10">
      <button
        onClick={() => navigate('/settings')}
        className="absolute right-4 top-4 text-xs text-slate-500 hover:text-slate-300"
      >
        ⚙️ Settings
      </button>

      {/* Robot + greeting */}
      <div className="flex flex-col items-center">
        <div className="text-6xl transition-transform duration-300">{moodDisplay.emoji}</div>
        <h1 className="mt-3 text-xl font-semibold text-white">
          {getGreeting()}{userName ? `, ${userName}` : ''}! What's up?
        </h1>
        <p className="mt-1 text-sm text-slate-500">{moodDisplay.message}</p>
      </div>

      {streak && streak.currentStreak > 0 && (
        <div className="mt-4 flex items-center gap-1.5 rounded-full bg-orange-500/10 px-3 py-1">
          <span className="text-sm">🔥</span>
          <span className="text-sm font-medium text-orange-400">
            {streak.currentStreak} day{streak.currentStreak !== 1 ? 's' : ''} streak
          </span>
        </div>
      )}

      {/* Hydration */}
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

      {/* Goals */}
      <div className="mt-6 w-full max-w-sm rounded-2xl bg-slate-800 p-5">
        <span className="text-sm font-medium text-white">🎯 Today's Goals</span>

        <div className="mt-3 space-y-2">
          {goals.map((goal) => (
            <div key={goal.id}>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => !goal.completed && handleCompleteGoal(goal.id)}
                  className={`flex h-5 w-5 shrink-0 items-center justify-center rounded border-2 ${
                    goal.completed ? 'border-emerald-500 bg-emerald-500' : 'border-slate-600 hover:border-slate-400'
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
                        className={`flex h-4 w-4 shrink-0 items-center justify-center rounded border-2 ${
                          sub.completed ? 'border-emerald-500 bg-emerald-500' : 'border-slate-600 hover:border-slate-400'
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
                <span className="text-xs uppercase tracking-wide text-indigo-400">{thing.category}</span>
                <h2 className="mt-2 text-lg font-semibold text-white">{thing.title}</h2>
                <p className="mt-2 text-sm text-slate-400">{thing.description}</p>

                {thing.tags.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-1.5">
                    {thing.tags.map((tag) => (
                      <span key={tag} className="rounded-full bg-slate-700 px-2.5 py-0.5 text-xs text-slate-300">
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

      <button onClick={logout} className="mt-10 text-sm text-slate-500 hover:text-slate-300">
        Log out
      </button>
    </div>
  );
}